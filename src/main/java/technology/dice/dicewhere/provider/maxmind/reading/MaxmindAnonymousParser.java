package technology.dice.dicewhere.provider.maxmind.reading;

import com.google.common.base.Splitter;
import inet.ipaddr.IPAddress;
import inet.ipaddr.IPAddressString;
import technology.dice.dicewhere.api.api.IP;
import technology.dice.dicewhere.utils.StringUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.*;

public class MaxmindAnonymousParser {
  private final Splitter splitter = Splitter.on(",");
  private final BufferedReader anonymousDatabase;
  private Optional<MaxmindAnonymous> lastFetched;

  public MaxmindAnonymousParser() {
    this.anonymousDatabase = null;
    this.lastFetched = Optional.empty();
  }

  public MaxmindAnonymousParser(BufferedReader anonymousDatabase) throws IOException {
    this.anonymousDatabase = anonymousDatabase;
    try {
      this.lastFetched =
          anonymousDatabase == null ? Optional.empty() : parseLine(anonymousDatabase.readLine());
    } catch (IOException e) {
      // TODO: what to do with this exception?
      throw e;
    }
  }

  public List<MaxmindAnonymous> fetchForRange(IPAddress ipAddressRange) {
    if (!lastFetched.isPresent()) {
      return Collections.emptyList();
    }
    List<MaxmindAnonymous> result = new ArrayList<>();
    IP rangeBoundStart = new IP(ipAddressRange.getLower().getBytes());
    IP rangeBoundEnd = new IP(ipAddressRange.getUpper().getBytes());
    do {
      lastFetched.ifPresent(
          l -> {
            result.add(fitToRange(l, ipAddressRange));
          });
      if (isFinishingAfter(rangeBoundEnd)) {
        readNextLine();
      }
    } while (lastOneInRange(rangeBoundStart, rangeBoundEnd));

    return Collections.unmodifiableList(result);
  }

  private void readNextLine() {
    try {
      this.lastFetched =
          anonymousDatabase == null ? Optional.empty() : parseLine(anonymousDatabase.readLine());
    } catch (IOException e) {
      this.lastFetched = Optional.empty();
    }
  }

  private boolean lastOneInRange(IP targetRangeStart, IP targetRangeEnd) {
    return lastFetched
        .map(
            a -> {
              IP lastFetchedRangeStart = a.getLowerBound();
              IP lastFetchedRangeEnd = a.getUpperBound();
              return (targetRangeStart.isLowerThan(lastFetchedRangeStart)
                      && targetRangeEnd.isGreaterThan(lastFetchedRangeStart))
                  || (targetRangeStart.isLowerThan(lastFetchedRangeEnd)
                      && targetRangeEnd.isGreaterThan(lastFetchedRangeEnd));
            })
        .orElse(false);
  }

  private boolean isFinishingAfter(IP targetRangeEnd) {
    return lastFetched
        .map(
            a -> {
              IP lastFetchedRangeEnd = a.getUpperBound();
              return lastFetchedRangeEnd.isGreaterThan(targetRangeEnd);
            })
        .orElse(false);
  }

  private MaxmindAnonymous fitToRange(MaxmindAnonymous lastLine, IPAddress ipAddressRange) {


    IP rangeLowerBound = new IP(ipAddressRange.getLower().getBytes());
    IP rangeUpperBound = new IP(ipAddressRange.getUpper().getBytes());

    IPAddress lastLineAddressRange = lastLine.getIpAddressRange();
//    lastLineAddressRange.iterator()
//    lastLineAddressRange.getLower().
//    new IPAd
    IP lastLineLowerBound = lastLine.getLowerBound();
    IP lastLineUpperBound = lastLine.getUpperBound();

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
      return lastLine;
    } else {
      return lastLine;
//      return MaxmindAnonymous.builder(lastLineLowerBound, lastLineUpperBound)
//          .isVpn(lastLine.isVpn())
//          .isTorExitProvider(lastLine.isTorExitProvider())
//          .isPublicProxy(lastLine.isPublicProxy())
//          .isHostingProvider(lastLine.isHostingProvider())
//          .isAnonymous(lastLine.isAnonymous())
//          .build();
    }
  }

  private Optional<MaxmindAnonymous> parseLine(String line) {
    Iterator<String> fieldsIterator = splitter.split(line).iterator();
    String range = StringUtils.removeQuotes(fieldsIterator.next());
    IPAddressString rangeString = new IPAddressString(range);
    if (rangeString.getAddress() == null) {
      return Optional.empty();
    }
    return Optional.of(
        MaxmindAnonymous.builder(rangeString.getAddress())
            .isAnonymous("1".equalsIgnoreCase(fieldsIterator.next()))
            .isVpn("1".equalsIgnoreCase(fieldsIterator.next()))
            .isHostingProvider("1".equalsIgnoreCase(fieldsIterator.next()))
            .isPublicProxy("1".equalsIgnoreCase(fieldsIterator.next()))
            .isTorExitProvider("1".equalsIgnoreCase(fieldsIterator.next()))
            .build());
  }
}
