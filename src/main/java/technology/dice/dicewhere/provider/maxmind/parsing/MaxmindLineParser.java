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
import technology.dice.dicewhere.provider.maxmind.reading.MaxmindLocation;
import technology.dice.dicewhere.reading.RawLine;
import technology.dice.dicewhere.utils.StringUtils;

import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
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

  public MaxmindLineParser(Map<String, MaxmindLocation> locationDictionary) {
    this.locationDictionary = locationDictionary;
  }

  @Override
  public Stream<ParsedLine> parse(RawLine rawLine, boolean retainOriginalLine) throws LineParsingException {
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

      return Stream.of(new ParsedLine(
          new IP(rangeStart.getBytes()),
          new IP(rangeEnd.getBytes()),
              IpInformation.builder().withCountryCodeAlpha2(StringUtils.removeQuotes(loc.getCountryCodeAlpha2())).withGeonameId(StringUtils.removeQuotes(geonameId)).withCity(StringUtils.removeQuotes(loc.getCity())).withLeastSpecificDivision(StringUtils.removeQuotes(loc.getLeastSpecificDivision())).withMostSpecificDivision(StringUtils.removeQuotes(loc.getMostSpecificDivision())).withPostcode(StringUtils.removeQuotes(postcode)).withStartOfRange(new IP(rangeStart.getBytes())).withEndOfRange(new IP(rangeEnd.getBytes())).withOriginalLine(retainOriginalLine ? rawLine.getLine() : null).build(),
          rawLine));
    } catch (NoSuchElementException e) {
      throw new LineParsingException(e, rawLine);
    }
  }
}
