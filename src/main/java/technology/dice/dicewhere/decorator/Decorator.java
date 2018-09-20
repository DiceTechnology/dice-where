package technology.dice.dicewhere.decorator;

import technology.dice.dicewhere.api.api.IP;
import technology.dice.dicewhere.api.api.IpInformation;
import technology.dice.dicewhere.utils.IPUtils;

import java.net.UnknownHostException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class Decorator<T extends DecoratorInformation> {

  private final Map<Integer, DecoratorDbReader<T>> databaseReaders;
  private final DecorationStrategy decorationStrategy;

  protected Decorator(
      Collection<DecoratorDbReader<T>> databaseReaders, DecorationStrategy decorationStrategy) {
    Objects.requireNonNull(databaseReaders);
    Objects.requireNonNull(decorationStrategy);
    if (databaseReaders.isEmpty()) {
      throw new IllegalArgumentException("Database readers can't be empty");
    }
    this.databaseReaders = new HashMap<>();
    int[] counter = new int[1];
    databaseReaders.forEach(
        r -> {
          this.databaseReaders.put(counter[0], r);
          counter[0]++;
        });
    this.decorationStrategy = decorationStrategy;
  }

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

  private List<T> mergeDecorationRanges(Collection<List<T>> source) {
    List<T> result = new ArrayList<>();

    List<T> sourceList =
        source
            .stream()
            .flatMap(Collection::stream)
            .sorted(
                Comparator.comparing((Function<T, IP>) DecoratorInformation::getRangeStart)
                    .thenComparing(DecoratorInformation::getRangeEnd))
            .collect(Collectors.toList());

    List<RangePoint<T>> splits = new ArrayList<>();
    Iterator<T> sourceIterator = sourceList.iterator();
    int counter = 0;
    while (sourceIterator.hasNext()) {
      T sourceItem = sourceIterator.next();
      int duplicates = 0;
      while (sourceIterator.hasNext()) {
        if (sourceList.get(counter + 1).equals(sourceItem)) {
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

    splits.sort(Comparator.comparing(RangePoint::getIp));



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

    int startPositionCounter = 1;
    for (int i = 1; i < splits.size(); i++) {
      int splitOccurances = splits.get(i - 1).getRangeInfo().getNumberOfMatches();
      if (startPositionCounter > 0
          && (startPositionCounter + splitOccurances - 1) >= filterThreshold) {
        try {
          result.add(
              splits
                  .get(i)
                  .getRangeInfo()
                  .withNumberOfMatches(startPositionCounter + splitOccurances - 1)
                  .withNewRange(
                      splits.get(i - 1).getIp(),
                      new IP(IPUtils.from(splits.get(i).getIp().getBytes()).getBytes())));
        } catch (UnknownHostException e) {
          // ignore, unlikely case. This would have broken somewhere else
        }
      }
      if (splits.get(i).isStart()) {
        startPositionCounter++;
      } else {
        startPositionCounter--;
      }
    }
    return result;
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

    List<DecorationRangePoint<Optional<T>>> splits = new ArrayList<>();
    for (T vdi : decoratorInfo) {
      splits.add(new DecorationRangePoint<>(vdi.getRangeStart(), true, Optional.of(vdi), original));
      splits.add(new DecorationRangePoint<>(vdi.getRangeEnd(), false, Optional.of(vdi), original));
    }

    splits.sort(Comparator.comparing(RangePoint::getIp));
    splits.addAll(fillRangeGaps(original, Collections.unmodifiableList(splits)));
    splits.sort(Comparator.comparing(RangePoint::getIp));

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
    Set<DecorationRangePoint<Optional<T>>> result = new HashSet<>();

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
    return result;
  }

  private Optional<IpInformation> fitIpInformationToSplits(
      IpInformation original, List<DecorationRangePoint<Optional<T>>> splits)
      throws UnknownHostException {
    IP lookupStart = original.getStartOfRange();
    IP lookupEnd = original.getEndOfRange();
    Optional<IP> foundStartingPositionForOriginal = Optional.empty();
    Optional<IP> foundEndingPositionForOriginal = Optional.empty();
    int inSplitRange = 0;
    for (int i = 0; i < splits.size(); i++) {
      if (inSplitRange <= 0 && splits.get(i).getIp().isGreaterThan(lookupStart)) {
        foundStartingPositionForOriginal = Optional.of(lookupStart);
        break;
      }
      if (splits.get(i).isStart()) {
        inSplitRange++;
      } else {
        inSplitRange--;
        lookupStart =
            new IP(IPUtils.from(splits.get(i).getIp().getBytes()).increment(1).getBytes());
      }
    }
    for (int i = splits.size() - 1; i >= 0; i--) {
      if (inSplitRange <= 0 && splits.get(i).getIp().isLowerThan(lookupEnd)) {
        foundEndingPositionForOriginal = Optional.of(lookupEnd);
        break;
      }
      if (!splits.get(i).isStart()) {
        inSplitRange++;
      } else {
        inSplitRange--;
        lookupEnd = new IP(IPUtils.from(splits.get(i).getIp().getBytes()).increment(-1).getBytes());
      }
    }
    if (foundStartingPositionForOriginal.isPresent()
        && foundEndingPositionForOriginal.isPresent()) {
      return Optional.of(
          IpInformation.builder(original)
              .withStartOfRange(foundStartingPositionForOriginal.get())
              .withEndOfRange(foundEndingPositionForOriginal.get())
              .build());
    } else {
      return Optional.empty();
    }
  }

  abstract IpInformation decorateIpInformationMatch(
      IpInformation ipInfo, DecorationRangePoint<Optional<T>> decorativeInfo, IP start, IP end);

  abstract IpInformation decorateIpInformationMiss(
      IpInformation ipInfo, DecorationRangePoint<Optional<T>> decorativeInfo, IP start, IP end);
}
