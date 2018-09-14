package technology.dice.dicewhere.parsing.provider.dbip;

import com.google.common.base.Splitter;
import com.google.common.net.InetAddresses;
import java.net.InetAddress;
import java.util.Iterator;
import java.util.NoSuchElementException;
import technology.dice.dicewhere.api.api.IP;
import technology.dice.dicewhere.api.api.IpInformation;
import technology.dice.dicewhere.api.exceptions.LineParsingException;
import technology.dice.dicewhere.parsing.LineParser;
import technology.dice.dicewhere.parsing.ParsedLine;
import technology.dice.dicewhere.reading.RawLine;
import technology.dice.dicewhere.utils.StringUtils;

public class DbIpLineParser implements LineParser {
  private static final Splitter splitter = Splitter.on(',');

  @Override
  public ParsedLine parse(RawLine line, boolean retainOriginalLine) throws LineParsingException {
    try {
      Iterable<String> fieldsIterable = splitter.split(line.getLine());
      Iterator<String> fieldsIterator = fieldsIterable.iterator();
      String rangeStartString = StringUtils.removeQuotes(fieldsIterator.next());
      String rangeEndString = StringUtils.removeQuotes(fieldsIterator.next());
      String countryCode = fieldsIterator.next();
      String leastSpecificDivision = fieldsIterator.hasNext() ? fieldsIterator.next() : null;
      String mostSpecificDivision = fieldsIterator.hasNext() ? fieldsIterator.next() : null;
      String city = fieldsIterator.hasNext() ? fieldsIterator.next() : null;
      String postCode = fieldsIterator.hasNext() ? fieldsIterator.next() : null;
      if (fieldsIterator.hasNext()) {
        fieldsIterator.next();
      }
      if (fieldsIterator.hasNext()) {
        fieldsIterator.next();
      }
      String geoname = fieldsIterator.hasNext() ? fieldsIterator.next() : null;
      InetAddress e = InetAddresses.forString(rangeEndString);
      InetAddress s = InetAddresses.forString(rangeStartString);
      IP startIp = new IP(s);
      IP endIp = new IP(e);
      return new ParsedLine(
		  startIp,
		  endIp,
		  new IpInformation(
			  StringUtils.removeQuotes(countryCode),
			  StringUtils.removeQuotes(geoname),
			  StringUtils.removeQuotes(city),
			  StringUtils.removeQuotes(leastSpecificDivision),
			  StringUtils.removeQuotes(mostSpecificDivision),
			  StringUtils.removeQuotes(postCode),
			  startIp,
			  endIp,
			  retainOriginalLine ? line.getLine() : null),
		  line);

    } catch (NoSuchElementException | IllegalArgumentException e) {
      throw new LineParsingException(e, line);
    }
  }
}
