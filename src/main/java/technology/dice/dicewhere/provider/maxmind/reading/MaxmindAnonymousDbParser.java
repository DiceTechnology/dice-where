/*
 * Copyright (C) 2018 - present by Dice Technology Ltd.
 *
 * Please see distribution for license.
 */

package technology.dice.dicewhere.provider.maxmind.reading;

import com.google.common.base.Splitter;
import inet.ipaddr.IPAddress;
import inet.ipaddr.IPAddressString;
import technology.dice.dicewhere.api.api.IP;
import technology.dice.dicewhere.utils.StringUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Iterator;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

public class MaxmindAnonymousDbParser {
  private final Splitter splitter = Splitter.on(",");
  private final BufferedReader ipV4AnonymousDatabase;
  private boolean ipV4DbExhausted = false;
  private final BufferedReader ipV6AnonymousDatabase;
  private Optional<MaxmindAnonymous> lastFetched;
  private final Function<MaxmindAnonymous, Boolean> filter;

  public MaxmindAnonymousDbParser() {
    this.ipV4AnonymousDatabase = null;
    this.ipV6AnonymousDatabase = null;
    this.lastFetched = Optional.empty();
    this.filter = maxmindAnonymous -> true;
  }

  public MaxmindAnonymousDbParser(
      BufferedReader ipV4AnonymousDatabase, BufferedReader ipV6AnonymousDatabase)
      throws IOException {
    this(ipV4AnonymousDatabase, ipV6AnonymousDatabase, maxmindAnonymous -> true);
  }

  public MaxmindAnonymousDbParser(
      BufferedReader ipV4AnonymousDatabase,
      BufferedReader ipV6AnonymousDatabase,
      Function<MaxmindAnonymous, Boolean> filter)
      throws IOException {
    this.ipV4AnonymousDatabase = ipV4AnonymousDatabase;
    this.ipV6AnonymousDatabase = ipV6AnonymousDatabase;
    this.ipV4AnonymousDatabase.readLine(); // first line is header
    this.ipV6AnonymousDatabase.readLine(); // first line is header
    this.filter = filter;
    this.readNextLine();
  }

  public Stream<MaxmindAnonymous> fetchForRange(IPAddress ipAddressRange) {
    if (!lastFetched.isPresent()) {
      return Stream.empty();
    }
    IP rangeBoundStart = new IP(ipAddressRange.getLower().getBytes());
    IP rangeBoundEnd = new IP(ipAddressRange.getUpper().getBytes());
    Stream.Builder<MaxmindAnonymous> result = Stream.builder();
    do {
      Optional<MaxmindAnonymous> resultRange =
          lastFetched.flatMap(l -> fitToRange(l, ipAddressRange));
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

    return result.build();
  }

  private Optional<String> readLine() throws IOException {
    if (!ipV4DbExhausted) {
      Optional<String> line = Optional.ofNullable(ipV4AnonymousDatabase.readLine());
      if (!line.isPresent()) {
        ipV4DbExhausted = true;
      } else {
        return line;
      }
    }
    return Optional.ofNullable(ipV6AnonymousDatabase.readLine());
  }

  private void readNextLine() {
    try {
      if (ipV4AnonymousDatabase != null && ipV6AnonymousDatabase != null) {
        while (true) {
          Optional<MaxmindAnonymous> parsedLine = readLine().flatMap(this::parseLine);
          if (!parsedLine.isPresent()) {
            this.lastFetched = Optional.empty();
            break;
          } else if (parsedLine.map(this.filter).orElse(false)) {
            this.lastFetched = parsedLine;
            break;
          } else {
            continue;
          }
        }
      }
    } catch (IOException e) {
      this.lastFetched = Optional.empty();
    }
  }

  private boolean isLastLineRangeContainingTargetRange(IP targetRangeStart, IP targetRangeEnd) {
    return lastFetched
        .map(
            a ->
                a.getRangeStart().isLowerThanOrEqual(targetRangeStart)
                    && a.getRangeEnd().isGreaterThanOrEqual(targetRangeEnd))
        .orElse(false);
  }

  private boolean isLastLineBeforeRange(IP targetRangeStart) {
    return lastFetched.map(a -> targetRangeStart.isGreaterThan(a.getRangeEnd())).orElse(false);
  }

  private boolean isLastLineAfterRange(IP targetEndStart) {
    return lastFetched.map(a -> targetEndStart.isLowerThan(a.getRangeStart())).orElse(false);
  }

  private boolean isLastLineInRange(IP targetRangeStart, IP targetRangeEnd) {
    return lastFetched
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
    return lastFetched.map(a -> a.getRangeEnd().isGreaterThan(targetRangeEnd)).orElse(false);
  }

  private Optional<MaxmindAnonymous> fitToRange(
      MaxmindAnonymous lastLine, IPAddress ipAddressRange) {

    IP rangeLowerBound = new IP(ipAddressRange.getLower().getBytes());
    IP rangeUpperBound = new IP(ipAddressRange.getUpper().getBytes());

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
      return Optional.of(
          MaxmindAnonymous.builder(lastLineLowerBound, lastLineUpperBound)
              .isVpn(lastLine.isVpn())
              .isTorExitProvider(lastLine.isTorExitProvider())
              .isPublicProxy(lastLine.isPublicProxy())
              .isHostingProvider(lastLine.isHostingProvider())
              .isAnonymous(lastLine.isAnonymous())
              .build());
    }
  }

  private Optional<MaxmindAnonymous> parseLine(String line) {
    Iterator<String> fieldsIterator = splitter.split(line).iterator();
    String range = StringUtils.removeQuotes(fieldsIterator.next());
    IPAddressString rangeString = new IPAddressString(range);
    if (rangeString.getAddress() == null) {
      return Optional.empty();
    }
    MaxmindAnonymous result =
        MaxmindAnonymous.builder(rangeString.getAddress())
            .isAnonymous("1".equalsIgnoreCase(fieldsIterator.next()))
            .isVpn("1".equalsIgnoreCase(fieldsIterator.next()))
            .isHostingProvider("1".equalsIgnoreCase(fieldsIterator.next()))
            .isPublicProxy("1".equalsIgnoreCase(fieldsIterator.next()))
            .isTorExitProvider("1".equalsIgnoreCase(fieldsIterator.next()))
            .build();
    return Optional.of(result);
  }
}
