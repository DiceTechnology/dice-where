/*
 * Copyright (C) 2018 - present by Dice Technology Ltd.
 *
 * Please see distribution for license.
 */

package technology.dice.dicewhere.decorator;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.jetbrains.annotations.NotNull;
import technology.dice.dicewhere.api.api.IP;
import technology.dice.dicewhere.api.api.IpInformation;
import technology.dice.dicewhere.utils.IPUtils;

import java.net.UnknownHostException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * Decorator is used to enrich IpInformation objects based on the data from the DecoratorDbReader.
 * Example: adding VPN information to IpInformation based on a DB different from the one used to
 * identify the location of the targeted IP
 */
public abstract class Decorator<T extends DecoratorInformation> {

  private final Map<Integer, DecoratorDbReader<T>> databaseReaders;
  private final DecorationStrategy decorationStrategy;

  Decorator(
      @NotNull Collection<DecoratorDbReader<T>> databaseReaders,
      @NotNull DecorationStrategy decorationStrategy) {
    Objects.requireNonNull(databaseReaders);
    Objects.requireNonNull(decorationStrategy);
    if (databaseReaders.isEmpty()) {
      throw new IllegalArgumentException("Database readers can't be empty");
    }
    AtomicInteger counter = new AtomicInteger(0);
    this.databaseReaders =
        databaseReaders
            .stream()
            .collect(
                ImmutableMap.toImmutableMap(e -> counter.getAndIncrement(), Function.identity()));
    this.decorationStrategy = decorationStrategy;
  }

  /**
   * Decorate the IpInformation with entries matching it's range from the Decorator's provided
   * databases;
   *
   * @param original the original IpInformation range
   * @return Stream of decorated IpInformation. It will split the original range into the matches
   *     and decorate each resulting range with IpInformation accordingly
   * @throws UnknownHostException if for some reason an IP operation fails because the IP isn't
   *     valid
   */
  public Stream<IpInformation> decorate(IpInformation original) throws UnknownHostException {
    Objects.requireNonNull(original);
    Map<Integer, List<T>> extraInformation = new HashMap<>();
    databaseReaders.forEach(
        (key, value) ->
            extraInformation.put(
                key, value.fetchForRange(original.getStartOfRange(), original.getEndOfRange())));

    return this.mergeIpInfoWithDecoratorInformation(
        original, mergeDecorationRanges(extraInformation.values()));
  }

  private List<T> mergeDecorationRanges(Collection<List<T>> found) throws UnknownHostException {

    List<T> foundRangesList =
        found
            .stream()
            .flatMap(Collection::stream)
            .sorted(
                Comparator.comparing((Function<T, IP>) DecoratorInformation::getRangeStart)
                    .thenComparing(DecoratorInformation::getRangeEnd))
            .collect(ImmutableList.toImmutableList());

    if (foundRangesList.isEmpty()) {
      return ImmutableList.of();
    }

    List<RangePoint<T>> splits = getRangePointsFromMatchedRanges(foundRangesList);

    int filterThreshold = getFilterThreshold();

    List<T> allValidRanges = getAllValidSplitRanges(splits, filterThreshold);

    if (allValidRanges.isEmpty()) {
      return ImmutableList.of();
    }

    List<T> mergedResults = new ArrayList<>();
    mergedResults.add(allValidRanges.get(0));
    // below can probably be optimised or incorporated in ::getAllValidSplitRanges
    for (int i = 1; i < allValidRanges.size(); i++) {
      if (allValidRanges
          .get(i)
          .getRangeStart()
          .isGreaterThan(
              IPUtils.increment(mergedResults.get(mergedResults.size() - 1).getRangeEnd()))) {
        mergedResults.add(allValidRanges.get(i));
      } else {
        IP start = mergedResults.get(mergedResults.size() - 1).getRangeStart();
        IP end = allValidRanges.get(i).getRangeEnd();
        mergedResults.set(mergedResults.size() - 1, allValidRanges.get(i).withNewRange(start, end));
      }
    }

    return ImmutableList.copyOf(mergedResults);
  }

  @NotNull
  private List<T> getAllValidSplitRanges(List<RangePoint<T>> splits, int filterThreshold)
      throws UnknownHostException {
    ImmutableList.Builder<T> allValidRanges = ImmutableList.builder();
    int rangeNestingLevel = 1;
    for (int i = 1; i < splits.size(); i++) {
      if (splits.get(i).isEnd()) {
        if (splits.get(i - 1).isStart()) {
          // 1) add the range
          if (Math.max(splits.get(i).getRangeInfo().getNumberOfMatches(), rangeNestingLevel)
              >= filterThreshold) {
            IP splitStart = splits.get(i - 1).getIp();
            IP splitEnd = new IP(IPUtils.from(splits.get(i).getIp().getBytes()).getBytes());
            allValidRanges.add(splits.get(i - 1).getRangeInfo().withNewRange(splitStart, splitEnd));
          }
          // 2) add the range splits previously in the list
          if (rangeNestingLevel > 1) {
            int rangeDiffCoverage =
                Math.max(
                    rangeNestingLevel - 1,
                    Math.min(
                            splits.get(i - 2).getRangeInfo().getNumberOfMatches(),
                            splits.get(i - 1).getRangeInfo().getNumberOfMatches())
                        - 1);
            if (rangeDiffCoverage >= filterThreshold) {
              IP start = splits.get(i - 2).getIp();
              IP nextSplitStart = splits.get(i - 1).getIp();
              if (new IP(IPUtils.from(start.getBytes()).increment(1).getBytes())
                  .isLowerThan(nextSplitStart)) {
                allValidRanges.add(
                    splits
                        .get(i - 2)
                        .getRangeInfo()
                        .withNumberOfMatches(
                            splits.get(i - 1).getRangeInfo().getNumberOfMatches() - 1)
                        .withNewRange(start, nextSplitStart));
              }
            }
          }
        } else {
          // 3) add region from this split_End to the previous split_End
          IP end = splits.get(i).getIp();
          IP prevSplitEnd = splits.get(i - 1).getIp();
          if (!end.equals(prevSplitEnd)
              && Math.max(splits.get(i).getRangeInfo().getNumberOfMatches(), rangeNestingLevel)
                  >= filterThreshold) {
            allValidRanges.add(
                splits
                    .get(i)
                    .getRangeInfo()
                    .withNewRange(
                        new IP(IPUtils.from(prevSplitEnd.getBytes()).increment(1).getBytes()),
                        end));
          }
        }
      } else if (splits.get(i).isStart() && rangeNestingLevel >= filterThreshold) {
        // 4) the gaps between ranges that are overlapped by a bigger range
        IP start = IPUtils.increment(splits.get(i - 1).getIp());
        IP end = IPUtils.decrement(splits.get(i).getIp());
        if (start.isLowerThan(end)) {
          allValidRanges.add(
              splits
                  .get(i)
                  .getRangeInfo()
                  .withNewRange(start, end)
                  .withNumberOfMatches(rangeNestingLevel));
        }
      }
      if (splits.get(i).isStart()) {
        rangeNestingLevel++;
      } else {
        rangeNestingLevel--;
      }
    }
    return ImmutableList.sortedCopyOf(
        Comparator.comparing(T::getRangeStart).thenComparing(T::getRangeEnd),
        allValidRanges.build());
  }

  private int getFilterThreshold() {
    int filterThreshold;
    switch (this.decorationStrategy) {
      case ALL:
        filterThreshold = this.databaseReaders.keySet().size();
        break;
      case MAJORITY:
        filterThreshold = (this.databaseReaders.keySet().size() + 1) / 2;
        break;
      case ANY:
      default:
        filterThreshold = 1;
    }
    return filterThreshold;
  }

  @NotNull
  private List<RangePoint<T>> getRangePointsFromMatchedRanges(List<T> foundRangesList) {
    List<RangePoint<T>> splits = new ArrayList<>();
    Iterator<T> sourceIterator = foundRangesList.iterator();
    int counter = 0;
    while (sourceIterator.hasNext()) {
      T sourceItem = sourceIterator.next();
      int duplicates = 0;
      while (sourceIterator.hasNext()) {
        if (foundRangesList.get(counter + 1).equals(sourceItem)) {
          duplicates++;
          counter++;
          sourceIterator.next();
        } else {
          break;
        }
      }
      T t = sourceItem.withNumberOfMatches(sourceItem.getNumberOfMatches() + duplicates);
      splits.add(new RangePoint<>(sourceItem.getRangeStart(), true, t));
      splits.add(new RangePoint<>(sourceItem.getRangeEnd(), false, t));
      counter++;
    }
    return ImmutableList.sortedCopyOf(Comparator.comparing(RangePoint::getIp), splits);
  }

  private Stream<IpInformation> mergeIpInfoWithDecoratorInformation(
      IpInformation original, List<T> decoratorInfo) throws UnknownHostException {

    if (decoratorInfo.isEmpty()) {
      return Stream.of(original);
    } else if (decoratorInfo.size() == 1
        && decoratorInfo.get(0).getRangeStart().equals(original.getStartOfRange())
        && decoratorInfo.get(0).getRangeEnd().equals(original.getEndOfRange())) {
      // the entire IpInformation is covered by one split range
      return Stream.of(
          decorateIpInformationMatch(
              original,
              new DecorationRangePoint<>(
                  decoratorInfo.get(0).getRangeStart(),
                  true,
                  Optional.of(decoratorInfo.get(0)),
                  original),
              original.getStartOfRange(),
              original.getEndOfRange()));
    }

    ImmutableList.Builder<DecorationRangePoint<Optional<T>>> splitsBuilder =
        ImmutableList.builder();
    for (T vdi : decoratorInfo) {
      splitsBuilder.add(
          new DecorationRangePoint<>(vdi.getRangeStart(), true, Optional.of(vdi), original));
      splitsBuilder.add(
          new DecorationRangePoint<>(vdi.getRangeEnd(), false, Optional.of(vdi), original));
    }

    splitsBuilder.addAll(
        fillRangeGaps(
            original,
            ImmutableList.sortedCopyOf(
                Comparator.comparing(RangePoint::getIp), splitsBuilder.build())));
    List<DecorationRangePoint<Optional<T>>> splits =
        ImmutableList.sortedCopyOf(Comparator.comparing(RangePoint::getIp), splitsBuilder.build());

    Stream.Builder<IpInformation> result = Stream.builder();
    int inRangeCounter = 1;
    for (int i = 1; i < splits.size(); i++) {
      if (inRangeCounter > 0) {
        IpInformation ipInfo = splits.get(i).getIpInformation();
        IP start;
        IP end;
        IpInformation decorated;
        if (inRangeCounter >= 1 && splits.get(i).getRangeInfo().isPresent()) {
          start = splits.get(i - 1).getIp();
          end = splits.get(i).getIp();
          // decorate
          decorated = decorateIpInformationMatch(ipInfo, splits.get(i), start, end);
        } else {
          start = splits.get(i - 1).getIp();
          end = splits.get(i).getIp();
          // only range changes
          decorated = decorateIpInformationMiss(ipInfo, splits.get(i), start, end);
        }
        result.add(decorated);
      }
      if (splits.get(i).isStart()) {
        inRangeCounter++;
      } else {
        inRangeCounter--;
      }
    }
    return result.build();
  }

  private Set<? extends DecorationRangePoint<Optional<T>>> fillRangeGaps(
      IpInformation original, List<DecorationRangePoint<Optional<T>>> splits)
      throws UnknownHostException {
    ImmutableSet.Builder<DecorationRangePoint<Optional<T>>> result = ImmutableSet.builder();

    IP lookupStart = original.getStartOfRange();
    IP lookupEnd = original.getEndOfRange();

    int inSplitRange = 0;
    for (int i = 0; i < splits.size(); i++) {
      if (inSplitRange <= 0 && splits.get(i).getIp().isGreaterThan(lookupStart)) {
        result.add(new DecorationRangePoint<>(lookupStart, true, Optional.empty(), original));
        result.add(
            new DecorationRangePoint<>(
                new IP(IPUtils.from(splits.get(i).getIp().getBytes()).increment(-1).getBytes()),
                false,
                Optional.empty(),
                original));
      }
      if (splits.get(i).isStart()) {
        inSplitRange++;
      } else {
        inSplitRange--;
        lookupStart =
            new IP(IPUtils.from(splits.get(i).getIp().getBytes()).increment(1).getBytes());
      }
    }
    if (lookupEnd.isGreaterThan(splits.get(splits.size() - 1).getIp())) {
      result.add(
          new DecorationRangePoint<>(
              new IP(
                  IPUtils.from(splits.get(splits.size() - 1).getIp().getBytes())
                      .increment(1)
                      .getBytes()),
              true,
              Optional.empty(),
              original));
      result.add(new DecorationRangePoint<>(lookupEnd, false, Optional.empty(), original));
    }
    return result.build();
  }

  abstract IpInformation decorateIpInformationMatch(
      IpInformation ipInfo, DecorationRangePoint<Optional<T>> decorativeInfo, IP start, IP end);

  abstract IpInformation decorateIpInformationMiss(
      IpInformation ipInfo, DecorationRangePoint<Optional<T>> decorativeInfo, IP start, IP end);
}
