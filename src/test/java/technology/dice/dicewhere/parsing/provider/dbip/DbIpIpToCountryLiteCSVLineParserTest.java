/*
 * Copyright (C) 2018 - present by Dice Technology Ltd.
 *
 * Please see distribution for license.
 */

package technology.dice.dicewhere.parsing.provider.dbip;

import com.google.common.net.InetAddresses;
import org.junit.Assert;
import org.junit.Test;
import technology.dice.dicewhere.api.api.IP;
import technology.dice.dicewhere.api.api.IpInformation;
import technology.dice.dicewhere.api.exceptions.LineParsingException;
import technology.dice.dicewhere.parsing.ParsedLine;
import technology.dice.dicewhere.provider.dbip.parsing.DbIpIpToCountryLiteCSVLineParser;
import technology.dice.dicewhere.reading.RawLine;

public class DbIpIpToCountryLiteCSVLineParserTest {
  @Test
  public void ipV4LineWithOriginalLine() throws LineParsingException {
    DbIpIpToCountryLiteCSVLineParser dbIpIpToCountryLiteCSVLineParser =
        new DbIpIpToCountryLiteCSVLineParser();
    String line = "1.52.0.0,1.55.255.255,VN";
    RawLine rawLine = new RawLine(line, 1);
    ParsedLine parsed = dbIpIpToCountryLiteCSVLineParser.parse(new RawLine(line, 1), true);
    ParsedLine expected =
        new ParsedLine(
            new IP(InetAddresses.forString("1.52.0.0")),
            new IP(InetAddresses.forString("1.55.255.255")),
            new IpInformation(
                "VN",
                null,
                null,
                null,
                null,
                null,
                new IP(InetAddresses.forString("1.52.0.0")),
                new IP(InetAddresses.forString("1.55.255.255")),
                line),
            rawLine);
    Assert.assertEquals(expected, parsed);
  }

  @Test
  public void ipV4LineWithoutOriginalLine() throws LineParsingException {
    DbIpIpToCountryLiteCSVLineParser dbIpIpToCountryLiteCSVLineParser =
        new DbIpIpToCountryLiteCSVLineParser();
    String line = "1.52.0.0,1.55.255.255,VN";
    RawLine rawLine = new RawLine(line, 1);
    ParsedLine parsed = dbIpIpToCountryLiteCSVLineParser.parse(new RawLine(line, 1), false);
    ParsedLine expected =
        new ParsedLine(
            new IP(InetAddresses.forString("1.52.0.0")),
            new IP(InetAddresses.forString("1.55.255.255")),
            new IpInformation(
                "VN",
                null,
                null,
                null,
                null,
                null,
                new IP(InetAddresses.forString("1.52.0.0")),
                new IP(InetAddresses.forString("1.55.255.255")),
                null),
            rawLine);
    Assert.assertEquals(expected, parsed);
  }

  @Test
  public void ipV6LineWithOriginal() throws LineParsingException {
    DbIpIpToCountryLiteCSVLineParser dbIpIpToCountryLiteCSVLineParser =
        new DbIpIpToCountryLiteCSVLineParser();
    String line = "2a0d:20c0::,2a0d:20c7:ffff:ffff:ffff:ffff:ffff:ffff,IT";
    RawLine rawLine = new RawLine(line, 1);
    ParsedLine parsed = dbIpIpToCountryLiteCSVLineParser.parse(new RawLine(line, 1), true);
    ParsedLine expected =
        new ParsedLine(
            new IP(InetAddresses.forString("2a0d:20c0:0000:0000:0000:0000:0000:0000")),
            new IP(InetAddresses.forString("2a0d:20c7:ffff:ffff:ffff:ffff:ffff:ffff")),
            new IpInformation(
                "IT",
                null,
                null,
                null,
                null,
                null,
                new IP(InetAddresses.forString("2a0d:20c0:0000:0000:0000:0000:0000:0000")),
                new IP(InetAddresses.forString("2a0d:20c7:ffff:ffff:ffff:ffff:ffff:ffff")),
                line),
            rawLine);
    Assert.assertEquals(expected, parsed);
  }

  @Test
  public void ipV6LineWithWithoutOriginalLine() throws LineParsingException {
    DbIpIpToCountryLiteCSVLineParser dbIpIpToCountryLiteCSVLineParser =
        new DbIpIpToCountryLiteCSVLineParser();
    String line = "2a0d:20c0::,2a0d:20c7:ffff:ffff:ffff:ffff:ffff:ffff,IT";
    RawLine rawLine = new RawLine(line, 1);
    ParsedLine parsed = dbIpIpToCountryLiteCSVLineParser.parse(new RawLine(line, 1), false);
    ParsedLine expected =
        new ParsedLine(
            new IP(InetAddresses.forString("2a0d:20c0:0000:0000:0000:0000:0000:0000")),
            new IP(InetAddresses.forString("2a0d:20c7:ffff:ffff:ffff:ffff:ffff:ffff")),
            new IpInformation(
                "IT",
                null,
                null,
                null,
                null,
                null,
                new IP(InetAddresses.forString("2a0d:20c0:0000:0000:0000:0000:0000:0000")),
                new IP(InetAddresses.forString("2a0d:20c7:ffff:ffff:ffff:ffff:ffff:ffff")),
                null),
            rawLine);
    Assert.assertEquals(expected, parsed);
  }

  @Test(expected = LineParsingException.class)
  public void wrongLineFormat() throws LineParsingException {
    DbIpIpToCountryLiteCSVLineParser dbIpIpToCountryLiteCSVLineParser =
        new DbIpIpToCountryLiteCSVLineParser();
    String line = "column1";
    try {
      dbIpIpToCountryLiteCSVLineParser.parse(new RawLine(line, 1), false);
    } catch (LineParsingException e) {
      Assert.assertEquals(line, e.getOffendingLine().getLine());
      Assert.assertEquals(1, e.getOffendingLine().getLineNumber());
      throw e;
    }
  }

  @Test(expected = LineParsingException.class)
  public void wrongStartIpFormat() throws LineParsingException {
    DbIpIpToCountryLiteCSVLineParser dbIpIpToCountryLiteCSVLineParser =
        new DbIpIpToCountryLiteCSVLineParser();

    String line =
        "uh-Oh,1.0.0.255,AU,Queensland,Brisbane,\"South Brisbane\",4101,-27.4748,153.017,2207259,10,Australia/Brisbane,\"APNIC Research and Development\",,";
    try {
      dbIpIpToCountryLiteCSVLineParser.parse(new RawLine(line, 1), false);
    } catch (LineParsingException e) {
      Assert.assertEquals(line, e.getOffendingLine().getLine());
      Assert.assertEquals(1, e.getOffendingLine().getLineNumber());
      throw e;
    }
  }

  @Test(expected = LineParsingException.class)
  public void wrongEndIpFormat() throws LineParsingException {
    DbIpIpToCountryLiteCSVLineParser dbIpIpToCountryLiteCSVLineParser =
        new DbIpIpToCountryLiteCSVLineParser();
    String line =
        "1.0.0.0,stop-Hammertime,AU,Queensland,Brisbane,\"South Brisbane\",4101,-27.4748,153.017,2207259,10,Australia/Brisbane,\"APNIC Research and Development\",,";
    try {
      dbIpIpToCountryLiteCSVLineParser.parse(new RawLine(line, 1), false);
    } catch (LineParsingException e) {
      Assert.assertEquals(line, e.getOffendingLine().getLine());
      Assert.assertEquals(1, e.getOffendingLine().getLineNumber());
      throw e;
    }
  }
}
