package technology.dice.dicewhere.reading.provider.maxmind;

import com.google.common.base.Splitter;
import technology.dice.dicewhere.utils.StringUtils;

import java.io.BufferedReader;
import java.util.Iterator;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MaxmindLocationsParser {
	private final Splitter splitter = Splitter.on(",");

	public Map<String, MaxmindLocation> locations(BufferedReader reader) {
		try (Stream<String> lines = reader.lines()) {
			return lines
					.map(line -> {
						Iterable<String> fieldsIterable = splitter.split(line);
						Iterator<String> fieldsIterator = fieldsIterable.iterator();
						String geonameId = fieldsIterator.next();
						fieldsIterator.next();
						fieldsIterator.next();
						fieldsIterator.next();
						String countryCode = fieldsIterator.next();
						if (fieldsIterator.hasNext()) {
							fieldsIterator.next();
						}
						if (fieldsIterator.hasNext()) {
							fieldsIterator.next();
						}

						String leastSpecificDivision = fieldsIterator.hasNext() ? fieldsIterator.next() : "";
						if (fieldsIterator.hasNext()) {
							fieldsIterator.next();
						}
						String mostSpecificDivision = fieldsIterator.hasNext() ? fieldsIterator.next() : "";
						String city = fieldsIterator.hasNext() ? fieldsIterator.next() : "";
						return new MaxmindLocation(
								StringUtils.removeQuotes(geonameId),
								StringUtils.removeQuotes(countryCode),
								StringUtils.removeQuotes(mostSpecificDivision),
								StringUtils.removeQuotes(leastSpecificDivision),
								StringUtils.removeQuotes(city)
						);
					})
					.collect(Collectors.toMap(
							e -> e.getGeonameId(),
							e -> e));
		}
	}
}
