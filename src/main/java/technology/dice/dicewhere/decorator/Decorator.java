package technology.dice.dicewhere.decorator;

import technology.dice.dicewhere.api.api.IP;
import technology.dice.dicewhere.api.api.IpInformation;
import technology.dice.dicewhere.utils.IPUtils;

import java.net.UnknownHostException;
import java.util.*;
import java.util.stream.Stream;

public abstract class Decorator<T extends DecoratorInformation> {

  protected final Map<Integer, DecoratorDbReader<T>> databaseReaders;
  protected final DecorationStrategy decorationStrategy;

  public Decorator(
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

  public Stream<IpInformation> decorate(IpInformation original) {
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

    List<RangePoint<T>> splits = new ArrayList<>();
    for (List<T> providerMatches : source) {
      for (T match : providerMatches) {
        splits.add(new RangePoint<>(match.getRangeStart(), true, match));
        splits.add(new RangePoint<>(match.getRangeEnd(), false, match));
      }
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
    for (int i = 1; i < splits.size() - 1; i++) {
      if (startPositionCounter >= filterThreshold) {
        try {
          splits
              .get(i)
              .getRangeInfo()
              .withNumberOfMatches(startPositionCounter)
              .withNewRange(
                  splits.get(i - 1).getIp(),
                  new IP(IPUtils.from(splits.get(i).getIp().getBytes()).increment(-1).getBytes()));
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

  Stream<IpInformation> mergeIpInfoWithDecoratorInformation(
      IpInformation original, List<T> decoratorInfo) {

    List<DecorationRangePoint<Optional<T>>> splits = new ArrayList<>();
    splits.add(
        new DecorationRangePoint<>(original.getStartOfRange(), true, Optional.empty(), original));
    splits.add(
        new DecorationRangePoint<>(original.getEndOfRange(), false, Optional.empty(), original));
    for (T vdi : decoratorInfo) {
      splits.add(new DecorationRangePoint<>(vdi.getRangeStart(), true, Optional.of(vdi), original));
      splits.add(new DecorationRangePoint<>(vdi.getRangeEnd(), false, Optional.of(vdi), original));
    }
    splits.sort(Comparator.comparing(RangePoint::getIp));

    Stream.Builder<IpInformation> result = Stream.builder();
    int startPositionCounter = 1;
    int vpnThreshold = 2;
    for (int i = 1; i < splits.size() - 1; i++) {
      if (startPositionCounter > 0) {
        try {
          IpInformation ipInfo = splits.get(i).getIpInformation();
          IP start = splits.get(i - 1).getIp();
          IP end = new IP(IPUtils.from(splits.get(i).getIp().getBytes()).increment(-1).getBytes());
          if (startPositionCounter >= vpnThreshold && splits.get(i).getRangeInfo().isPresent()) {
            // decorate
            IpInformation decorated =
                decorateIpInformation(ipInfo, splits.get(i).getRangeInfo().get(), start, end);
            result.add(decorated);
          } else {
            // only change ranges
            result.add(
                IpInformation.builder(ipInfo).withStartOfRange(start).withEndOfRange(end).build());
          }
        } catch (UnknownHostException e) {
          // ignore, unlikely case. This would have broken somewhere else
          result.add(original);
          break;
        }
      }
      if (splits.get(i).isStart()) {
        startPositionCounter++;
      } else {
        startPositionCounter--;
      }
    }
    return result.build();
  }

  abstract IpInformation decorateIpInformation(
      IpInformation ipInfo, T decorativeInfo, IP start, IP end);
}
