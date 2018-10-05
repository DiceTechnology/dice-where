/*
 * Copyright (C) 2018 - present by Dice Technology Ltd.
 *
 * Please see distribution for license.
 */

package technology.dice.dicewhere.decorator;

import com.google.common.collect.ImmutableList;
import technology.dice.dicewhere.api.api.IP;
import technology.dice.dicewhere.api.exceptions.DecoratorDatabaseOutOfOrderException;
import technology.dice.dicewhere.utils.IPUtils;

import java.net.UnknownHostException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Reads one line at a time from the provided DB. Only hold one line in memory and stop reading when
 * an IP is reached, that is outside of the requested one
 *
 * <p>T lastFetched holds the last line that was read from the DB The reader will keep reading
 * until it reaches the end of the file or the end of the requested ip range;
 */
public abstract class DecoratorDbReader<T extends DecoratorInformation> {
  private T lastFetched;

  protected Optional<T> getLastFetched() {
    return Optional.ofNullable(lastFetched);
  }

  /**
   * The last successfully parsed line read from the provided decorating DB
   * @param lastFetched set the last fetched line to the passed argument
   */
  protected void setLastFetched(T lastFetched) {
    this.lastFetched = lastFetched;
  }

  // TODO: javadoc
  public final List<T> fetchForRange(IP rangeBoundStart, IP rangeBoundEnd) {
    if (!getLastFetched().isPresent()) {
      return ImmutableList.of();
    }
    Stream.Builder<T> result = Stream.builder();
    do {
      Optional<T> resultRange =
          getLastFetched().flatMap(l -> fitToRange(l, rangeBoundStart, rangeBoundEnd));
      if (resultRange.isPresent()) {
        result.add(resultRange.get());
        if (isLastLineRangeContainingTargetRange(rangeBoundStart, rangeBoundEnd)) {
          break;
        }
      }
      if (!isFinishingAfter(rangeBoundEnd)) {
        Optional<T> lastReadLine = readNextLine();
        if (lastReadLine.isPresent() && getLastFetched().isPresent()) {
          if (lastReadLine
              .get()
              .getRangeStart()
              .isLowerThanOrEqual(getLastFetched().get().getRangeEnd())) {
            try {
            throw new DecoratorDatabaseOutOfOrderException(
                String.format(
                    "Ranges out of line for %s - %s",
                    IPUtils.from(lastReadLine.get().getRangeStart()),
                    IPUtils.from(lastReadLine.get().getRangeEnd())));
            } catch (UnknownHostException e) {
              throw new RuntimeException(e);
            }
          }
        }
        this.setLastFetched(lastReadLine.orElse(null));
      }
    } while (isLastLineBeforeRange(rangeBoundEnd)
        || isLastLineInRange(rangeBoundStart, rangeBoundEnd));

    return result.build().collect(ImmutableList.toImmutableList());
  }

  protected abstract Optional<T> readNextLine();

  private boolean isLastLineRangeContainingTargetRange(IP targetRangeStart, IP targetRangeEnd) {
    return getLastFetched()
        .map(
            a ->
                a.getRangeStart().isLowerThanOrEqual(targetRangeStart)
                    && a.getRangeEnd().isGreaterThanOrEqual(targetRangeEnd))
        .orElse(false);
  }

  private boolean isLastLineBeforeRange(IP targetRangeStart) {
    return getLastFetched().map(a -> targetRangeStart.isGreaterThan(a.getRangeEnd())).orElse(false);
  }

  private boolean isLastLineAfterRange(IP targetEndStart) {
    return getLastFetched().map(a -> targetEndStart.isLowerThan(a.getRangeStart())).orElse(false);
  }

  private boolean isLastLineInRange(IP targetRangeStart, IP targetRangeEnd) {
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

  private boolean isFinishingAfter(IP targetRangeEnd) {
    return getLastFetched().map(a -> a.getRangeEnd().isGreaterThan(targetRangeEnd)).orElse(false);
  }

  /**
   * crops the range of the T to the currently requested range, if it's outside of it.
   *
   * @param lastLine
   * @param rangeLowerBound
   * @param rangeUpperBound
   * @return
   */
  private Optional<T> fitToRange(T lastLine, IP rangeLowerBound, IP rangeUpperBound) {
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
