/*
 * Copyright (C) 2018 - present by Dice Technology Ltd.
 *
 * Please see distribution for license.
 */

package technology.dice.dicewhere.provider.dbip.parsing;

import com.google.common.base.Splitter;
import com.google.common.net.InetAddresses;
import java.net.InetAddress;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Optional;
import technology.dice.dicewhere.api.api.IP;
import technology.dice.dicewhere.api.api.IpInformation;
import technology.dice.dicewhere.api.exceptions.LineParsingException;
import technology.dice.dicewhere.decorator.Decorator;
import technology.dice.dicewhere.decorator.DecoratorInformation;
import technology.dice.dicewhere.parsing.LineParser;
import technology.dice.dicewhere.reading.RawLine;
import technology.dice.dicewhere.utils.StringUtils;

/**
 * Parser for DB-Ip's <a href="https://db-ip.com/db/ip-to-location-isp">IP to Location + ISP</a> db
 * in CSV file format<br>
 * At the time of writing this class, the documentation did not match the contents of the file. This
 * parser, therefore, does not follow the link above, but rather the following format:<br>
 * <br>
 *
 * <p>ip_start,ip_end,country,stateprov,district,city,zipcode,latitude,longitude,geoname_id,timezone_offset,timeszone_name,isp_name,connection_type,organization_name
 */
public class DbIpIpToLocationAndIspCSVLineParser extends LineParser {
  private static final Splitter splitter = Splitter.on(',');

  private final Decorator<? extends DecoratorInformation> decorator;

  public DbIpIpToLocationAndIspCSVLineParser() {
    this(null);
  }

  public DbIpIpToLocationAndIspCSVLineParser(Decorator<? extends DecoratorInformation> decorator) {
    this.decorator = decorator;
  }

  @Override
  public Optional<Decorator<? extends DecoratorInformation>> getDecorator() {
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
      return IpInformation.builder()
          .withCountryCodeAlpha2(StringUtils.removeQuotes(countryCode))
          .withGeonameId(StringUtils.removeQuotes(geoname))
          .withCity(StringUtils.removeQuotes(city))
          .withLeastSpecificDivision(StringUtils.removeQuotes(leastSpecificDivision))
          .withMostSpecificDivision(StringUtils.removeQuotes(mostSpecificDivision))
          .withPostcode(StringUtils.removeQuotes(postCode))
          .withStartOfRange(startIp)
          .withEndOfRange(endIp)
          .withOriginalLine(retainOriginalLine ? line.getLine() : null)
          .build();

    } catch (NoSuchElementException | IllegalArgumentException e) {
      throw new LineParsingException(e, line);
    }
  }
}
