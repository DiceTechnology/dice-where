package technology.dice.dicewhere.parsing.provider.dbip;

import com.google.common.base.Splitter;
import com.google.common.net.InetAddresses;
import technology.dice.dicewhere.api.api.IP;
import technology.dice.dicewhere.api.api.IPInformation;
import technology.dice.dicewhere.api.exceptions.LineParsingException;
import technology.dice.dicewhere.parsing.LineParser;
import technology.dice.dicewhere.parsing.ParsedLine;
import technology.dice.dicewhere.reading.RawLine;

import java.net.InetAddress;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class DbIpLineParser implements LineParser {
	private static final Splitter splitter = Splitter.on(',');

	@Override
	public ParsedLine parse(RawLine line, boolean retainOriginalLine) throws LineParsingException {
		try {
			Iterable<String> fieldsIterable = splitter.split(line.getLine());
			Iterator<String> fieldsIterator = fieldsIterable.iterator();
			String rangeStartString = this.removeQuotes(fieldsIterator.next());
			String rangeEndString = this.removeQuotes(fieldsIterator.next());
			String countryCode = fieldsIterator.next();
			String leastSpecificDivision = fieldsIterator.hasNext() ? fieldsIterator.next() : "";
			String mostSpecificDivision = fieldsIterator.hasNext() ? fieldsIterator.next() : "";
			String city = fieldsIterator.hasNext() ? fieldsIterator.next() : "";
			String postCode = fieldsIterator.hasNext() ? fieldsIterator.next() : "";
			if (fieldsIterator.hasNext()) {
				fieldsIterator.next();
			}
			if (fieldsIterator.hasNext()) {
				fieldsIterator.next();
			}
			String geoname = fieldsIterator.hasNext() ? fieldsIterator.next() : "";
			try {
				InetAddress e = InetAddresses.forString(rangeEndString);
				InetAddress s = InetAddresses.forString(rangeStartString);
				IP startIp = new IP(s);
				IP endIp = new IP(e);
				ParsedLine result = new ParsedLine(
						startIp,
						endIp,
						new IPInformation(
								this.removeQuotes(countryCode),
								this.removeQuotes(geoname),
								this.removeQuotes(city),
								this.removeQuotes(leastSpecificDivision),
								this.removeQuotes(mostSpecificDivision),
								this.removeQuotes(postCode),
								startIp,
								endIp,
								retainOriginalLine ? line.getLine() : null
						),
						line
				);
				return result;
			} catch (IllegalArgumentException e) {
				throw new LineParsingException(e, line);
			}

		} catch (NoSuchElementException e) {
			throw new LineParsingException(e, line);
		}
	}
}