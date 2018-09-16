/*
 * Copyright (C) 2018 - present by Dice Technology Ltd.
 *
 * Please see distribution for license.
 */

package technology.dice.dicewhere.provider.maxmind.parsing;

import com.google.common.base.Splitter;
import inet.ipaddr.IPAddress;
import inet.ipaddr.IPAddressString;
import technology.dice.dicewhere.api.api.IP;
import technology.dice.dicewhere.api.api.IpInformation;
import technology.dice.dicewhere.api.exceptions.LineParsingException;
import technology.dice.dicewhere.parsing.LineParser;
import technology.dice.dicewhere.parsing.ParsedLine;
import technology.dice.dicewhere.provider.maxmind.reading.MaxmindAnonymous;
import technology.dice.dicewhere.provider.maxmind.reading.MaxmindAnonymousDbParser;
import technology.dice.dicewhere.provider.maxmind.reading.MaxmindLocation;
import technology.dice.dicewhere.reading.RawLine;
import technology.dice.dicewhere.utils.StringUtils;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Parser for any Maxmind database.<br>
 * Maxmind's databases present the fields in increasing order of granularity. A more precise
 * database will contain all fields of a less precise database, adding the extra precision at the
 * end. This class will parse fields up to the level of postcode, presented by the GeoIP2-City
 * databases.<br>
 * This parser parses both the Commercial and Lite versions of Maxmind's databases.
 */
public class MaxmindLineParser implements LineParser {
  private final Splitter splitter = Splitter.on(",");
  private final Map<String, MaxmindLocation> locationDictionary;
  private final MaxmindAnonymousDbParser vpnDbParser;

  public MaxmindLineParser(
      Map<String, MaxmindLocation> locationDictionary, MaxmindAnonymousDbParser vpnDbParser) {

    this.locationDictionary = locationDictionary;
    this.vpnDbParser = vpnDbParser;
  }

  public MaxmindLineParser(Map<String, MaxmindLocation> locationDictionary) {
    this(locationDictionary, new MaxmindAnonymousDbParser());
  }

  @Override
  public Stream<ParsedLine> parse(RawLine rawLine, boolean retainOriginalLine)
      throws LineParsingException {
    try {
      Iterable<String> fieldsIterable = splitter.split(rawLine.getLine());
      Iterator<String> fieldsIterator = fieldsIterable.iterator();
      String range = StringUtils.removeQuotes(fieldsIterator.next());
      String geonameId = fieldsIterator.next();
      String geonameIdRepresented = fieldsIterator.next();
      if (fieldsIterator.hasNext()) {
        fieldsIterator.next();
      }
      if (fieldsIterator.hasNext()) {
        fieldsIterator.next();
      }
      if (fieldsIterator.hasNext()) {
        fieldsIterator.next();
      }
      String postcode = fieldsIterator.hasNext() ? fieldsIterator.next() : null;
      MaxmindLocation loc = locationDictionary.get(geonameId);
      if (loc == null) {
        loc = locationDictionary.get(geonameIdRepresented);
      }
      if (loc == null) {
        loc = MaxmindLocation.UNKNOWN;
      }

      IPAddressString rangeString = new IPAddressString(range);
      if (rangeString.getAddress() == null) {
        throw new LineParsingException("Invalid IP range", rawLine);
      }

      IPAddress rangeStart = rangeString.getAddress().getLower();
      IPAddress rangeEnd = rangeString.getAddress().getUpper();

      IpInformation ipInfo =
          IpInformation.builder()
              .withCountryCodeAlpha2(StringUtils.removeQuotes(loc.getCountryCodeAlpha2()))
              .withGeonameId(StringUtils.removeQuotes(geonameId))
              .withCity(StringUtils.removeQuotes(loc.getCity()))
              .withLeastSpecificDivision(StringUtils.removeQuotes(loc.getLeastSpecificDivision()))
              .withMostSpecificDivision(StringUtils.removeQuotes(loc.getMostSpecificDivision()))
              .withPostcode(StringUtils.removeQuotes(postcode))
              .withStartOfRange(new IP(rangeStart.getBytes()))
              .withEndOfRange(new IP(rangeEnd.getBytes()))
              .withOriginalLine(retainOriginalLine ? rawLine.getLine() : null)
              .build();

      return parseNestedRanges(rangeString.getAddress(), ipInfo, rawLine);
    } catch (NoSuchElementException e) {
      throw new LineParsingException(e, rawLine);
    }
  }

  private Stream<ParsedLine> parseNestedRanges(
      IPAddress ipAddressRange, IpInformation ipInfo, RawLine rawLine) {

    Stream<MaxmindAnonymous> vpnRanges = vpnDbParser.fetchForRange(ipAddressRange);

    return mergeIpInfoWithVpnData(
        Collections.unmodifiableList(vpnRanges.collect(Collectors.toList())),
        ipAddressRange,
        ipInfo,
        rawLine);
  }

  /**
   * This method produces multiples ranges out of the main IP range split by the VPN ranges
   *
   * @param vpnRanges
   * @param ipAddressRange
   * @param ipInfo
   * @param rawLine
   * @return
   */
  private Stream<ParsedLine> mergeIpInfoWithVpnData(
      List<MaxmindAnonymous> vpnRanges,
      IPAddress ipAddressRange,
      IpInformation ipInfo,
      RawLine rawLine) {

    if (vpnRanges.isEmpty()) {
      IP rangeStart = new IP(ipAddressRange.getLower().getBytes());
      IP rangeEnd = new IP(ipAddressRange.getUpper().getBytes());
      return Stream.of(new ParsedLine(rangeStart, rangeEnd, ipInfo, rawLine));
    }

    Stream.Builder<ParsedLine> result = Stream.builder();

    Iterator<? extends IPAddress> ipRangeIterator = ipAddressRange.getIterable().iterator();
    IPAddress segmentStartIp = ipAddressRange;
    IPAddress previousIp = ipAddressRange;
    for (MaxmindAnonymous vpnRange : vpnRanges) {
      // find the last ip from the iterator that is before the current vpn range
      while (ipRangeIterator.hasNext()) {
        IPAddress lastIp = ipRangeIterator.next();
        IP lastIpUpperBound = new IP(lastIp.getUpper().getBytes());
        if (lastIpUpperBound.isGreaterThanOrEqual(vpnRange.getRangeStart())) {
          // insert the segment that is not vpn
          System.out.println(
              String.format(
                  "Adding source info for %s - %s",
                  segmentStartIp.getLower(), previousIp.getUpper()));
          IP s = new IP(segmentStartIp.getLower().getBytes());
          IP e = new IP(previousIp.getUpper().getBytes());
          result.add(
              new ParsedLine(
                  s,
                  e,
                  IpInformation.builder(ipInfo)
                      .withStartOfRange(s)
                      .withEndOfRange(e)
                      .isVpn(false)
                      .build(),
                  rawLine));
          break;
        }
        previousIp = lastIp;
      }
      // insert vpn segment
      IpInformation vpnIpInfo =
          IpInformation.builder(ipInfo)
              .isVpn(vpnRange.isVpn())
              .withStartOfRange(vpnRange.getRangeStart())
              .withEndOfRange(vpnRange.getRangeEnd())
              .build();
      System.out.println(
          String.format(
              "Adding VPN info for %s - %s",
              new String(vpnRange.getRangeStart().getBytes()),
              new String(vpnRange.getRangeEnd().getBytes())));
      result.add(
          new ParsedLine(
              vpnIpInfo.getStartOfRange(), vpnIpInfo.getEndOfRange(), vpnIpInfo, rawLine));

      // move iterator to the last ip in the current vpn range
      while (ipRangeIterator.hasNext()) {
        IPAddress lastIp = ipRangeIterator.next();
        IP lastRangeUpperBound = new IP(lastIp.getUpper().getBytes());
        if (lastRangeUpperBound.isGreaterThan(vpnRange.getRangeEnd())) {
          previousIp = segmentStartIp = lastIp;
          System.out.println(String.format("New segment start: %s ", lastIp.getLower()));
          break;
        }
      }
    }
    IP segmentLowerBoud = new IP(segmentStartIp.getLower().getBytes());
    if (segmentLowerBoud.isGreaterThan(vpnRanges.get(vpnRanges.size() - 1).getRangeEnd())) {
      IP s = new IP(segmentStartIp.getLower().getBytes());
      result.add(
          new ParsedLine(
              s,
              ipInfo.getEndOfRange(),
              IpInformation.builder(ipInfo).withStartOfRange(s).isVpn(false).build(),
              rawLine));
    }

    return result.build();
  }
}
