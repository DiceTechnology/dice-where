package technology.dice.dicewhere.decorator;

import technology.dice.dicewhere.api.api.IP;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class DecoratorDbReader<T extends DecoratorInformation> {
  private T lastFetched;

  Optional<T> getLastFetched() {
    return Optional.ofNullable(lastFetched);
  }

  protected void setLastFetched(T lastFetched) {
    this.lastFetched = lastFetched;
  }

  public final List<T> fetchForRange(IP rangeBoundStart, IP rangeBoundEnd) {
    if (!getLastFetched().isPresent()) {
      return Collections.EMPTY_LIST;
    }
    Stream.Builder<T> result = Stream.builder();
    do {
      Optional<T> resultRange = getLastFetched().flatMap(l -> fitToRange(l, rangeBoundStart, rangeBoundEnd));
      if (resultRange.isPresent()) {
        result.add(resultRange.get());
        if (isLastLineRangeContainingTargetRange(rangeBoundStart, rangeBoundEnd)) {
          break;
        }
      }
      if (!isFinishingAfter(rangeBoundEnd)) {
        readNextLine();
      }
    } while (isLastLineBeforeRange(rangeBoundEnd)
        || isLastLineInRange(rangeBoundStart, rangeBoundEnd));

    return result.build().collect(Collectors.toList());
  }

  protected abstract void readNextLine();

  boolean isLastLineRangeContainingTargetRange(IP targetRangeStart, IP targetRangeEnd) {
    return getLastFetched()
        .map(
            a ->
                a.getRangeStart().isLowerThanOrEqual(targetRangeStart)
                    && a.getRangeEnd().isGreaterThanOrEqual(targetRangeEnd))
        .orElse(false);
  }

  boolean isLastLineBeforeRange(IP targetRangeStart) {
    return getLastFetched().map(a -> targetRangeStart.isGreaterThan(a.getRangeEnd())).orElse(false);
  }

  boolean isLastLineAfterRange(IP targetEndStart) {
    return getLastFetched().map(a -> targetEndStart.isLowerThan(a.getRangeStart())).orElse(false);
  }

  boolean isLastLineInRange(IP targetRangeStart, IP targetRangeEnd) {
    return getLastFetched()
        .map(
            a -> {
              IP lastFetchedRangeStart = a.getRangeStart();
              IP lastFetchedRangeEnd = a.getRangeEnd();
              return (targetRangeStart.isLowerThanOrEqual(lastFetchedRangeStart)
                      && targetRangeEnd.isGreaterThanOrEqual(lastFetchedRangeStart))
                  || (targetRangeStart.isLowerThanOrEqual(lastFetchedRangeEnd)
                      && targetRangeEnd.isGreaterThanOrEqual(lastFetchedRangeEnd));
            })
        .orElse(false);
  }

  boolean isFinishingAfter(IP targetRangeEnd) {
    return getLastFetched().map(a -> a.getRangeEnd().isGreaterThan(targetRangeEnd)).orElse(false);
  }

  /**
   * crops the range of the T to the currently requested range, if it's outside of it.
   * @param lastLine
   * @param rangeLowerBound
   * @param rangeUpperBound
   * @return
   */
  protected Optional<T> fitToRange(T lastLine, IP rangeLowerBound, IP rangeUpperBound) {
    IP lastLineLowerBound = lastLine.getRangeStart();
    IP lastLineUpperBound = lastLine.getRangeEnd();

    if (isLastLineBeforeRange(rangeLowerBound) || isLastLineAfterRange(rangeUpperBound)) {
      return Optional.empty();
    }

    boolean toModify = false;
    if (lastLineLowerBound.isLowerThan(rangeLowerBound)) {
      lastLineLowerBound = rangeLowerBound;
      toModify = true;
    }
    if (lastLineUpperBound.isGreaterThan(rangeUpperBound)) {
      lastLineUpperBound = rangeUpperBound;
      toModify = true;
    }
    if (!toModify) {
      return Optional.of(lastLine);
    } else {
      return Optional.of(updateRange(lastLine, lastLineLowerBound, lastLineUpperBound));
    }
  }

  private T updateRange(T original, IP lowerBound, IP upperBound) {
    return original.withNewRange(lowerBound, upperBound);
  }

  protected abstract Optional<T> parseDbLine(String line);
}
