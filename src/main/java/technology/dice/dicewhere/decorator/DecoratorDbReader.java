/*
 * Copyright (C) 2018 - present by Dice Technology Ltd.
 *
 * Please see distribution for license.
 */

package technology.dice.dicewhere.decorator;

import com.google.common.collect.ImmutableList;
import org.mapdb.DB;
import org.mapdb.DBException;
import org.mapdb.DBMaker;
import technology.dice.dicewhere.api.api.IP;
import technology.dice.dicewhere.api.exceptions.DecoratorDatabaseOutOfOrderException;
import technology.dice.dicewhere.lineprocessing.serializers.IPSerializer;
import technology.dice.dicewhere.utils.IPUtils;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.*;
import java.util.stream.Stream;

/**
 * Reads one line at a time from the provided DB. Only hold one line in memory and stop reading when
 * an IP is reached, that is outside of the requested one
 *
 * <p>T lastFetched holds the last line that was read from the DB The reader will keep reading until
 * it reaches the end of the file or the end of the requested ip range;
 */
public abstract class DecoratorDbReader<T extends DecoratorInformation> {
  private NavigableMap<IP, T> db;

  protected abstract void prepareDataSource() throws IOException;

  protected void init() throws IOException {
    prepareDataSource();
    DB db =
        DBMaker.tempFileDB()
            .checksumHeaderBypass()
            .fileLockDisable()
            .fileMmapEnable()
            .fileChannelEnable()
            .transactionEnable()
            .fileDeleteAfterClose()
            .make();

    DB.TreeMapSink<IP, T> sink =
        db.treeMap(
                this.getClass().getName(),
                new IPSerializer(),
                new DecoratorInformationSerializer<T>())
            .createFromSink();

    Optional<T> prevDbValue = Optional.empty();
    for (; ; ) {
      Optional<T> nextLine = readNextLine();
      try {
        if (!nextLine.isPresent()) {
          break;
        }
        T dbValue = nextLine.get();
        if (prevDbValue
            .map(pv -> dbValue.getRangeStart().isGreaterThan(pv.getRangeEnd()))
            .orElse(true)) {
          prevDbValue = Optional.of(dbValue);
          sink.put(dbValue.getRangeStart(), dbValue);
        } else {
          throw new DecoratorDatabaseOutOfOrderException(
              String.format(
                  "DB out of order for IP range %s - %s",
                  IPUtils.from(nextLine.get().getRangeStart()),
                  IPUtils.from(nextLine.get().getRangeEnd())));
        }
      } catch (DBException.NotSorted e) {
        throw new DecoratorDatabaseOutOfOrderException(
            String.format(
                "DB out of order for IP range %s - %s",
                IPUtils.from(nextLine.get().getRangeStart()),
                IPUtils.from(nextLine.get().getRangeEnd())));
      }
    }
    this.db = sink.create();
  }

  // TODO: javadoc
  public final List<T> fetchForRange(IP rangeBoundStart, IP rangeBoundEnd) {
    Objects.requireNonNull(db);
    Stream.Builder<T> result = Stream.builder();
    IP seekStart = rangeBoundStart;
    Optional<Map.Entry<IP, T>> biggestPriorToRange =
        Optional.ofNullable(this.db.floorEntry(seekStart));
    if (biggestPriorToRange.isPresent()
        && biggestPriorToRange.get().getValue().getRangeEnd().isGreaterThanOrEqual(seekStart)) {

      seekStart = biggestPriorToRange.get().getValue().getRangeEnd();
      biggestPriorToRange
          .flatMap(e -> fitToRange(e.getValue(), rangeBoundStart, rangeBoundEnd))
          .ifPresent(result::add);
    }
    for (; ; ) {
      Optional<Map.Entry<IP, T>> entry = Optional.ofNullable(this.db.ceilingEntry(seekStart));
      if (!entry.isPresent()) {
        break;
      }
      if (entry.get().getValue().getRangeStart().isGreaterThan(rangeBoundEnd)) {
        break;
      }
      entry
          .flatMap(e -> fitToRange(e.getValue(), rangeBoundStart, rangeBoundEnd))
          .ifPresent(result::add);
      if (entry.get().getValue().getRangeEnd().isGreaterThan(seekStart)) {
        try {
          seekStart = IPUtils.increment(entry.get().getValue().getRangeEnd());
        } catch (UnknownHostException e) {
          // This is unlikely but if happens it indicates the DB is corrupted containing invalid
          // data. All records should be in ascending order
          throw new RuntimeException(e);
        }
      } else {
        break;
      }
    }
    return result.build().collect(ImmutableList.toImmutableList());
  }

  protected abstract Optional<T> readNextLine();

  private boolean isLastLineBeforeRange(T dbEntry, IP targetRangeStart) {
    return targetRangeStart.isGreaterThan(dbEntry.getRangeEnd());
  }

  private boolean isLastLineAfterRange(T dbEntry, IP targetEndStart) {
    return targetEndStart.isLowerThan(dbEntry.getRangeStart());
  }

  /**
   * crops the range of the T to the currently requested range, if it's outside of it.
   *
   * @param dbEntry
   * @param rangeLowerBound
   * @param rangeUpperBound
   * @return
   */
  private Optional<T> fitToRange(T dbEntry, IP rangeLowerBound, IP rangeUpperBound) {
    IP lastLineLowerBound = dbEntry.getRangeStart();
    IP lastLineUpperBound = dbEntry.getRangeEnd();

    if (isLastLineBeforeRange(dbEntry, rangeLowerBound)
        || isLastLineAfterRange(dbEntry, rangeUpperBound)) {
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
      return Optional.of(dbEntry);
    } else {
      return Optional.of(updateRange(dbEntry, lastLineLowerBound, lastLineUpperBound));
    }
  }

  private T updateRange(T original, IP lowerBound, IP upperBound) {
    return original.withNewRange(lowerBound, upperBound);
  }

  protected abstract Optional<T> parseDbLine(String line);
}
