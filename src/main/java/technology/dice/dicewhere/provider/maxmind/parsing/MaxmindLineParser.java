package technology.dice.dicewhere.provider.maxmind.parsing;

import com.google.common.base.Splitter;
import inet.ipaddr.IPAddress;
import inet.ipaddr.IPAddressString;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.*;
import java.util.stream.Stream;

import technology.dice.dicewhere.api.api.IP;
import technology.dice.dicewhere.api.api.IpInformation;
import technology.dice.dicewhere.api.exceptions.LineParsingException;
import technology.dice.dicewhere.parsing.LineParser;
import technology.dice.dicewhere.parsing.ParsedLine;
import technology.dice.dicewhere.provider.maxmind.reading.MaxmindAnonymous;
import technology.dice.dicewhere.provider.maxmind.reading.MaxmindAnonymousParser;
import technology.dice.dicewhere.reading.RawLine;
import technology.dice.dicewhere.provider.maxmind.reading.MaxmindLocation;
import technology.dice.dicewhere.utils.StringUtils;

public class MaxmindLineParser implements LineParser {
  private final Splitter splitter = Splitter.on(",");
  private final Map<String, MaxmindLocation> locationDictionary;
  private final MaxmindAnonymousParser anonymousDatabaseParser;

  public MaxmindLineParser(
      Map<String, MaxmindLocation> locationDictionary,
      MaxmindAnonymousParser anonymousDatabaseParser) {

    this.locationDictionary = locationDictionary;
    this.anonymousDatabaseParser = anonymousDatabaseParser;
  }

  public MaxmindLineParser(Map<String, MaxmindLocation> locationDictionary) {
    this(locationDictionary, new MaxmindAnonymousParser());
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
      IPAddress ipAddress, IpInformation ipInfo, RawLine rawLine) {
    IPAddress rangeStart = ipAddress.getLower();
    IPAddress rangeEnd = ipAddress.getUpper();

    List<MaxmindAnonymous> anonymousDatabaseEntries =
        anonymousDatabaseParser.fetchForRange(ipAddress);

    return Stream.of(
        new ParsedLine(
            new IP(rangeStart.getBytes()), new IP(rangeEnd.getBytes()), ipInfo, rawLine));
  }

  private Stream<ParsedLine> mergeIpInfoAndAnonymousDatabase(
      List<MaxmindAnonymous> anonymousDatabaseEntries,
      IPAddress ipAddress,
      IpInformation ipInfo,
      RawLine rawLine) {

    IP rangeStart = new IP(ipAddress.getLower().getBytes());
    IP rangeEnd = new IP(ipAddress.getUpper().getBytes());
    if (anonymousDatabaseEntries.isEmpty()) {
      return Stream.of(new ParsedLine(rangeStart, rangeEnd, ipInfo, rawLine));
    }

    Stream.Builder<ParsedLine> result = Stream.builder();

    if (rangeStart.isLowerThan(anonymousDatabaseEntries.get(0).getLowerBound())) {
      result.add(
          new ParsedLine(
              rangeStart,
              new IP(
                  anonymousDatabaseEntries
                      .get(0)
                      .getIpAddressRange()
                      .increment(-1)
                      .getLower()
                      .getBytes()),
              ipInfo,
              rawLine));
    }
    anonymousDatabaseEntries.forEach(
        a ->
            result.add(
                new ParsedLine(
                    a.getLowerBound(),
                    a.getUpperBound(),
                    IpInformation.builder(ipInfo).isVpn(a.isVpn()).build(),
                    rawLine)));
    if (rangeEnd.isGreaterThan(
        anonymousDatabaseEntries.get(anonymousDatabaseEntries.size() - 1).getUpperBound())) {
      IPAddress lastEntryAddress =
          anonymousDatabaseEntries.get(anonymousDatabaseEntries.size() - 1).getIpAddressRange();
      result.add(
          new ParsedLine(
              new IP(
                  lastEntryAddress
                      .increment(lastEntryAddress.getCount().longValue() + 1L)
                      .getBytes()),
              rangeEnd,
              ipInfo,
              rawLine));
    }

    return result.build();
  }
}
