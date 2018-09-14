/*
 * Copyright (C) 2018 - present by Dice Technology Ltd.
 *
 * Please see distribution for license.
 */

package technology.dice.dicewhere.provider.dbip.parsing;

import com.google.common.base.Splitter;
import com.google.common.net.InetAddresses;
import technology.dice.dicewhere.api.api.IP;
import technology.dice.dicewhere.api.api.IpInformation;
import technology.dice.dicewhere.api.exceptions.LineParsingException;
import technology.dice.dicewhere.parsing.LineParser;
import technology.dice.dicewhere.parsing.ParsedLine;
import technology.dice.dicewhere.reading.RawLine;
import technology.dice.dicewhere.utils.StringUtils;

import java.net.InetAddress;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Parser for DB-Ip's <a href="https://db-ip.com/db/download/ip-to-city-lite">City Lite</a> db
 * in CSV file format<br>
 */
public class DbIpIpToCityLiteCSVLineParser implements LineParser {
  private static final Splitter splitter = Splitter.on(',');

  @Override
  public ParsedLine parse(RawLine line, boolean retainOriginalLine) throws LineParsingException {
    try {
      Iterable<String> fieldsIterable = splitter.split(line.getLine());
      Iterator<String> fieldsIterator = fieldsIterable.iterator();
      String rangeStartString = StringUtils.removeQuotes(fieldsIterator.next());
      String rangeEndString = StringUtils.removeQuotes(fieldsIterator.next());
      fieldsIterator.next(); // skip continent
      String countryCode = fieldsIterator.next();
      String leastSpecificDivision = fieldsIterator.hasNext() ? fieldsIterator.next() : null;
      String city = fieldsIterator.hasNext() ? fieldsIterator.next() : null;
      InetAddress e = InetAddresses.forString(rangeEndString);
      InetAddress s = InetAddresses.forString(rangeStartString);
      IP startIp = new IP(s);
      IP endIp = new IP(e);
      ParsedLine result =
          new ParsedLine(
              startIp,
              endIp,
              new IpInformation(
                  StringUtils.removeQuotes(countryCode),
                  null,
                  StringUtils.removeQuotes(city),
                  StringUtils.removeQuotes(leastSpecificDivision),
                  null,
                  null,
                  startIp,
                  endIp,
                  retainOriginalLine ? line.getLine() : null),
              line);
      return result;

    } catch (NoSuchElementException | IllegalArgumentException e) {
      throw new LineParsingException(e, line);
    }
  }
}
