package technology.dice.dicewhere.parsing.provider.maxmind;


import com.google.common.base.Splitter;
import inet.ipaddr.IPAddress;
import inet.ipaddr.IPAddressString;
import technology.dice.dicewhere.api.api.IP;
import technology.dice.dicewhere.api.api.IPInformation;
import technology.dice.dicewhere.api.exceptions.LineParsingException;
import technology.dice.dicewhere.parsing.LineParser;
import technology.dice.dicewhere.parsing.ParsedLine;
import technology.dice.dicewhere.reading.RawLine;
import technology.dice.dicewhere.reading.provider.maxmind.MaxmindLocation;

import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;

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
			String range = removeQuotes(fieldsIterator.next());
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
			String postcode = fieldsIterator.hasNext() ? fieldsIterator.next() : "";
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
					new IPInformation(
							removeQuotes(loc.getCountryCodeAlpha2()),
							removeQuotes(geonameId),
							removeQuotes(loc.getCity()),
							removeQuotes(loc.getLeastSpecificDivision()),
							removeQuotes(loc.getMostSpecificDivision()),
							removeQuotes(postcode),
							new IP(rangeStart.getBytes()),
							new IP(rangeEnd.getBytes()),
							retainOriginalLine ? rawLine.getLine() : null
					),
					rawLine

			);
		} catch (NoSuchElementException e) {
			throw new LineParsingException(e, rawLine);
		}
	}
}
