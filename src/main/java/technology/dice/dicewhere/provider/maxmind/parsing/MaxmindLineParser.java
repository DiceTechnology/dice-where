/*
 * Copyright (C) 2018 - present by Dice Technology Ltd.
 *
 * Please see distribution for license.
 */

package technology.dice.dicewhere.provider.maxmind.parsing;

import com.google.common.base.Splitter;
import inet.ipaddr.IPAddress;
import inet.ipaddr.IPAddressString;
import org.jetbrains.annotations.NotNull;
import technology.dice.dicewhere.api.api.IP;
import technology.dice.dicewhere.api.api.IpInformation;
import technology.dice.dicewhere.api.exceptions.LineParsingException;
import technology.dice.dicewhere.parsing.LineParser;
import technology.dice.dicewhere.parsing.ParsedLine;
import technology.dice.dicewhere.provider.maxmind.reading.MaxmindAnonymous;
import technology.dice.dicewhere.provider.maxmind.reading.MaxmindAnonymousDbParser;
import technology.dice.dicewhere.provider.maxmind.reading.MaxmindLocation;
import technology.dice.dicewhere.reading.RawLine;
import technology.dice.dicewhere.utils.IPUtils;
import technology.dice.dicewhere.utils.StringUtils;

import java.net.UnknownHostException;
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
    } catch (NoSuchElementException | UnknownHostException e) {
      throw new LineParsingException(e, rawLine);
    }
  }

  private Stream<ParsedLine> parseNestedRanges(
      IPAddress ipAddressRange, IpInformation ipInfo, RawLine rawLine) throws UnknownHostException {

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
   * @param vpnRanges IP ranges that have been identified as VPNs
   * @param ipAddressRange IP range from the last read line
   * @param ipInfo Information about the IP range from the least read line
   * @param rawLine the raw information from the last read line
   * @return
   */
  private Stream<ParsedLine> mergeIpInfoWithVpnData(
      List<MaxmindAnonymous> vpnRanges,
      IPAddress ipAddressRange,
      IpInformation ipInfo,
      RawLine rawLine)
      throws UnknownHostException {

    IP rangeStart = new IP(ipAddressRange.getLower().getBytes());
    IP rangeEnd = new IP(ipAddressRange.getUpper().getBytes());
    if (vpnRanges.isEmpty()) {
      return Stream.of(new ParsedLine(rangeStart, rangeEnd, ipInfo, rawLine));
    }

    Stream.Builder<ParsedLine> result = Stream.builder();

    IP nextIpForResult = new IP(rangeStart.getBytes());
    for (MaxmindAnonymous vpnRange : vpnRanges) {
      if (vpnRange.getRangeStart().isGreaterThan(nextIpForResult)) {
        IPAddress endRangeNoneVpn =
                IPUtils.from(vpnRange.getRangeStart().getBytes()).increment(-1);

        IP e = new IP(endRangeNoneVpn.getUpper().getBytes());
        result.add(buildParsedLine(ipInfo, rawLine, e, nextIpForResult, false));
      }

      IpInformation vpnIpInfo =
              IpInformation.builder(ipInfo)
                      .isVpn(vpnRange.isVpn())
                      .withStartOfRange(vpnRange.getRangeStart())
                      .withEndOfRange(vpnRange.getRangeEnd())
                      .build();
      result.add(
              buildParsedLine(
                      vpnIpInfo, rawLine, vpnIpInfo.getEndOfRange(), vpnIpInfo.getStartOfRange(), true));
      nextIpForResult =
              new IP(
                      IPUtils.from(vpnRange.getRangeEnd().getBytes())
                              .increment(1)
                              .getLower()
                              .getBytes());
    }

    if (nextIpForResult.isLowerThan(rangeEnd)) {
      result.add(buildParsedLine(ipInfo, rawLine, rangeEnd, nextIpForResult, false));
    }

    return result.build();
  }

  @NotNull
  private ParsedLine buildParsedLine(
      IpInformation ipInfo, RawLine rawLine, IP rangeEnd, IP rangeStart, Boolean isVpn) {
    return new ParsedLine(
        rangeStart,
        rangeEnd,
        IpInformation.builder(ipInfo)
            .withStartOfRange(rangeStart)
            .withEndOfRange(rangeEnd)
            .isVpn(Optional.ofNullable(isVpn))
            .build(),
        rawLine);
  }
}
