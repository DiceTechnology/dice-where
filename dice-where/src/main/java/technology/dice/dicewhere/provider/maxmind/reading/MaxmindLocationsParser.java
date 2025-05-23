/*
 * Copyright (C) 2018 - present by Dice Technology Ltd.
 *
 * Please see distribution for license.
 */

package technology.dice.dicewhere.provider.maxmind.reading;

import com.google.common.base.Splitter;
import java.io.BufferedReader;
import java.util.Iterator;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import technology.dice.dicewhere.utils.StringUtils;

public class MaxmindLocationsParser {
  private final Splitter splitter = Splitter.on(",");

  public Map<String, MaxmindLocation> locations(BufferedReader reader) {
    try (Stream<String> lines = reader.lines()) {
      return lines
          .map(
              line -> {
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

                String leastSpecificDivision =
                    fieldsIterator.hasNext() ? fieldsIterator.next() : "";
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
                    StringUtils.removeQuotes(city));
              })
          .collect(Collectors.toMap(MaxmindLocation::getCityGeonameId, e -> e));
    }
  }
}
