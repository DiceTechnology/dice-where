package technology.dice.dicewhere.parsing.provider.maxmind;

import com.google.common.base.Splitter;
import inet.ipaddr.IPAddress;
import inet.ipaddr.IPAddressString;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import technology.dice.dicewhere.api.api.IP;
import technology.dice.dicewhere.api.api.IpInformation;
import technology.dice.dicewhere.api.exceptions.LineParsingException;
import technology.dice.dicewhere.parsing.LineParser;
import technology.dice.dicewhere.parsing.ParsedLine;
import technology.dice.dicewhere.reading.RawLine;
import technology.dice.dicewhere.reading.provider.maxmind.MaxmindLocation;
import technology.dice.dicewhere.utils.StringUtils;

public class MaxmindLineParser implements LineParser {
  private final Splitter splitter = Splitter.on(",");
  private final Map<String, MaxmindLocation> locationDictionary;

  public MaxmindLineParser(Map<String, MaxmindLocation> locationDictionary) {
    this.locationDictionary = locationDictionary;
  }

  @Override
  public ParsedLine parse(RawLine rawLine, boolean retainOriginalLine) throws LineParsingException {
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

      return new ParsedLine(
          new IP(rangeStart.getBytes()),
          new IP(rangeEnd.getBytes()),
              IpInformation.builder().withCountryCodeAlpha2(StringUtils.removeQuotes(loc.getCountryCodeAlpha2())).withGeonameId(StringUtils.removeQuotes(geonameId)).withCity(StringUtils.removeQuotes(loc.getCity())).withLeastSpecificDivision(StringUtils.removeQuotes(loc.getLeastSpecificDivision())).withMostSpecificDivision(StringUtils.removeQuotes(loc.getMostSpecificDivision())).withPostcode(StringUtils.removeQuotes(postcode)).withStartOfRange(new IP(rangeStart.getBytes())).withEndOfRange(new IP(rangeEnd.getBytes())).withOriginalLine(retainOriginalLine ? rawLine.getLine() : null).build(),
          rawLine);
    } catch (NoSuchElementException e) {
      throw new LineParsingException(e, rawLine);
    }
  }
}