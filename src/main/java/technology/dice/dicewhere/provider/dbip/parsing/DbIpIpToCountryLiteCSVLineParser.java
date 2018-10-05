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
import technology.dice.dicewhere.decorator.Decorator;
import technology.dice.dicewhere.decorator.DecoratorInformation;
import technology.dice.dicewhere.parsing.LineParser;
import technology.dice.dicewhere.reading.RawLine;
import technology.dice.dicewhere.utils.StringUtils;

import java.net.InetAddress;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Optional;

/**
 * Parser for DB-Ip's <a href="https://db-ip.com/db/download/ip-to-country-lite">Country Lite</a> db
 * in CSV file format<br>
 */
public class DbIpIpToCountryLiteCSVLineParser extends LineParser {
  private static final Splitter splitter = Splitter.on(',');
  private final Decorator<? extends DecoratorInformation> decorator;

  public DbIpIpToCountryLiteCSVLineParser() {
    this(null);
  }

  public DbIpIpToCountryLiteCSVLineParser(Decorator<? extends DecoratorInformation> decorator) {
    this.decorator = decorator;
  }

  @Override
  protected Optional<Decorator<? extends DecoratorInformation>> getDecorator() {
    return Optional.ofNullable(decorator);
  }

  @Override
  protected IpInformation parseLine(RawLine line, boolean retainOriginalLine)
      throws LineParsingException {
    try {
      Iterable<String> fieldsIterable = splitter.split(line.getLine());
      Iterator<String> fieldsIterator = fieldsIterable.iterator();
      String rangeStartString = StringUtils.removeQuotes(fieldsIterator.next());
      String rangeEndString = StringUtils.removeQuotes(fieldsIterator.next());
      String countryCode = fieldsIterator.next();
      InetAddress e = InetAddresses.forString(rangeEndString);
      InetAddress s = InetAddresses.forString(rangeStartString);
      IP startIp = new IP(s);
      IP endIp = new IP(e);
      return IpInformation.builder()
              .withCountryCodeAlpha2(StringUtils.removeQuotes(countryCode))
              .withStartOfRange(startIp)
              .withEndOfRange(endIp)
              .withOriginalLine(retainOriginalLine ? line.getLine() : null)
              .build();

    } catch (NoSuchElementException | IllegalArgumentException e) {
      throw new LineParsingException(e, line);
    }
  }
}
