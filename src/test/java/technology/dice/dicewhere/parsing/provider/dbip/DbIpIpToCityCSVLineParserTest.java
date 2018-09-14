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
import technology.dice.dicewhere.provider.dbip.parsing.DbIpIpToCityCSVLineParser;
import technology.dice.dicewhere.provider.dbip.parsing.DbIpIpToLocationAndIspCSVLineParser;
import technology.dice.dicewhere.reading.RawLine;

public class DbIpIpToCityCSVLineParserTest {
  @Test
  public void ipV4LineWithOriginalLine() throws LineParsingException {
    DbIpIpToCityCSVLineParser dbIpIpToCityCSVLineParser = new DbIpIpToCityCSVLineParser();
    String line = "1.8.154.0,1.8.155.255,AS,CN,Beijing,Beijing,39.904,116.408";
    RawLine rawLine = new RawLine(line, 1);
    ParsedLine parsed = dbIpIpToCityCSVLineParser.parse(new RawLine(line, 1), true);
    ParsedLine expected =
        new ParsedLine(
            new IP(InetAddresses.forString("1.8.154.0")),
            new IP(InetAddresses.forString("1.8.155.255")),
            new IpInformation(
                "CN",
                null,
                "Beijing",
                "Beijing",
                null,
                null,
                new IP(InetAddresses.forString("1.8.154.0")),
                new IP(InetAddresses.forString("1.8.155.255")),
                line),
            rawLine);
    Assert.assertEquals(expected, parsed);
  }

  @Test
  public void ipV4LineWithoutOriginalLine() throws LineParsingException {
    DbIpIpToCityCSVLineParser dbIpIpToCityCSVLineParser = new DbIpIpToCityCSVLineParser();
    String line = "1.8.154.0,1.8.155.255,AS,CN,Beijing,Beijing,39.904,116.408";
    RawLine rawLine = new RawLine(line, 1);
    ParsedLine parsed = dbIpIpToCityCSVLineParser.parse(new RawLine(line, 1), false);
    ParsedLine expected =
        new ParsedLine(
            new IP(InetAddresses.forString("1.8.154.0")),
            new IP(InetAddresses.forString("1.8.155.255")),
            new IpInformation(
                "CN",
                null,
                "Beijing",
                "Beijing",
                null,
                null,
                new IP(InetAddresses.forString("1.8.154.0")),
                new IP(InetAddresses.forString("1.8.155.255")),
                null),
            rawLine);
    Assert.assertEquals(expected, parsed);
  }

  @Test
  public void ipV6LineWithOriginal() throws LineParsingException {
    DbIpIpToCityCSVLineParser dbIpIpToCityCSVLineParser = new DbIpIpToCityCSVLineParser();
    String line =
        "2c0f:ff80::,2c0f:ff80:ffff:ffff:ffff:ffff:ffff:ffff,AF,ZA,Gauteng,\"Sandton (Woodmead)\",-26.0561,28.0696";
    RawLine rawLine = new RawLine(line, 1);
    ParsedLine parsed = dbIpIpToCityCSVLineParser.parse(new RawLine(line, 1), true);
    ParsedLine expected =
        new ParsedLine(
            new IP(InetAddresses.forString("2C0F:FF80:0000:0000:0000:0000:0000:0000")),
            new IP(InetAddresses.forString("2c0f:ff80:ffff:ffff:ffff:ffff:ffff:ffff")),
            new IpInformation(
                "ZA",
                null,
                "Sandton (Woodmead)",
                "Gauteng",
                null,
                null,
                new IP(InetAddresses.forString("2C0F:FF80:0000:0000:0000:0000:0000:0000")),
                new IP(InetAddresses.forString("2c0f:ff80:ffff:ffff:ffff:ffff:ffff:ffff")),
                line),
            rawLine);
    Assert.assertEquals(expected, parsed);
  }

  @Test
  public void ipV6LineWithWithoutOriginalLine() throws LineParsingException {
    DbIpIpToCityCSVLineParser dbIpIpToCityCSVLineParser = new DbIpIpToCityCSVLineParser();
    String line =
        "2c0f:ff80::,2c0f:ff80:ffff:ffff:ffff:ffff:ffff:ffff,AF,ZA,Gauteng,\"Sandton (Woodmead)\",-26.0561,28.0696";
    RawLine rawLine = new RawLine(line, 1);
    ParsedLine parsed = dbIpIpToCityCSVLineParser.parse(new RawLine(line, 1), false);
    ParsedLine expected =
        new ParsedLine(
            new IP(InetAddresses.forString("2C0F:FF80:0000:0000:0000:0000:0000:0000")),
            new IP(InetAddresses.forString("2c0f:ff80:ffff:ffff:ffff:ffff:ffff:ffff")),
            new IpInformation(
                "ZA",
                null,
                "Sandton (Woodmead)",
                "Gauteng",
                null,
                null,
                new IP(InetAddresses.forString("2C0F:FF80:0000:0000:0000:0000:0000:0000")),
                new IP(InetAddresses.forString("2c0f:ff80:ffff:ffff:ffff:ffff:ffff:ffff")),
                null),
            rawLine);
    Assert.assertEquals(expected, parsed);
  }

  @Test(expected = LineParsingException.class)
  public void wrongLineFormat() throws LineParsingException {
    DbIpIpToCityCSVLineParser dbIpIpToCityCSVLineParser = new DbIpIpToCityCSVLineParser();
    String line = "column1,column2,column3";
    try {
      dbIpIpToCityCSVLineParser.parse(new RawLine(line, 1), false);
    } catch (LineParsingException e) {
      Assert.assertEquals(line, e.getOffendingLine().getLine());
      Assert.assertEquals(1, e.getOffendingLine().getLineNumber());
      throw e;
    }
  }

  @Test(expected = LineParsingException.class)
  public void wrongStartIpFormat() throws LineParsingException {
    DbIpIpToCityCSVLineParser dbIpIpToCityCSVLineParser = new DbIpIpToCityCSVLineParser();

    String line =
        "uh-Oh,1.0.0.255,AU,Queensland,Brisbane,\"South Brisbane\",4101,-27.4748,153.017,2207259,10,Australia/Brisbane,\"APNIC Research and Development\",,";
    try {
      dbIpIpToCityCSVLineParser.parse(new RawLine(line, 1), false);
    } catch (LineParsingException e) {
      Assert.assertEquals(line, e.getOffendingLine().getLine());
      Assert.assertEquals(1, e.getOffendingLine().getLineNumber());
      throw e;
    }
  }

  @Test(expected = LineParsingException.class)
  public void wrongEndIpFormat() throws LineParsingException {
    DbIpIpToCityCSVLineParser dbIpIpToCityCSVLineParser = new DbIpIpToCityCSVLineParser();
    String line =
        "1.0.0.0,stop-Hammertime,AU,Queensland,Brisbane,\"South Brisbane\",4101,-27.4748,153.017,2207259,10,Australia/Brisbane,\"APNIC Research and Development\",,";
    try {
      dbIpIpToCityCSVLineParser.parse(new RawLine(line, 1), false);
    } catch (LineParsingException e) {
      Assert.assertEquals(line, e.getOffendingLine().getLine());
      Assert.assertEquals(1, e.getOffendingLine().getLineNumber());
      throw e;
    }
  }

  @Test
  public void onlyCountryIPV4() throws LineParsingException {
    DbIpIpToLocationAndIspCSVLineParser dbIpIpToLocationAndIspCSVLineParser =
        new DbIpIpToLocationAndIspCSVLineParser();
    String line = "\"1.4.128.0\",\"1.4.255.255\",\"TH\"";
    RawLine rawLine = new RawLine(line, 1);
    ParsedLine parsed = dbIpIpToLocationAndIspCSVLineParser.parse(new RawLine(line, 1), false);
    ParsedLine expected =
        new ParsedLine(
            new IP(InetAddresses.forString("1.4.128.0")),
            new IP(InetAddresses.forString("1.4.255.255")),
            new IpInformation(
                "TH",
                null,
                null,
                null,
                null,
                null,
                new IP(InetAddresses.forString("1.4.128.0")),
                new IP(InetAddresses.forString("1.4.255.255")),
                null),
            rawLine);
    Assert.assertEquals(expected, parsed);
  }

  @Test
  public void onlyCountryIPV6() throws LineParsingException {
    DbIpIpToLocationAndIspCSVLineParser dbIpIpToLocationAndIspCSVLineParser =
        new DbIpIpToLocationAndIspCSVLineParser();
    String line = "\"2a0c:3800:400::\",\"2a0c:3800:400:ffff:ffff:ffff:ffff:ffff\",\"PT\"";
    RawLine rawLine = new RawLine(line, 1);
    ParsedLine parsed = dbIpIpToLocationAndIspCSVLineParser.parse(new RawLine(line, 1), false);
    ParsedLine expected =
        new ParsedLine(
            new IP(InetAddresses.forString("2a0c:3800:400::")),
            new IP(InetAddresses.forString("2a0c:3800:400:ffff:ffff:ffff:ffff:ffff")),
            new IpInformation(
                "PT",
                null,
                null,
                null,
                null,
                null,
                new IP(InetAddresses.forString("2a0c:3800:400::")),
                new IP(InetAddresses.forString("2a0c:3800:400:ffff:ffff:ffff:ffff:ffff")),
                null),
            rawLine);
    Assert.assertEquals(expected, parsed);
  }
}
