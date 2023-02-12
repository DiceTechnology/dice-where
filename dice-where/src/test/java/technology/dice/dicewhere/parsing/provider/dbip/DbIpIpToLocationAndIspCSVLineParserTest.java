/*
 * Copyright (C) 2018 - present by Dice Technology Ltd.
 *
 * Please see distribution for license.
 */

package technology.dice.dicewhere.parsing.provider.dbip;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.google.common.net.InetAddresses;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import technology.dice.dicewhere.api.api.IP;
import technology.dice.dicewhere.api.api.IpInformation;
import technology.dice.dicewhere.api.exceptions.LineParsingException;
import technology.dice.dicewhere.parsing.ParsedLine;
import technology.dice.dicewhere.provider.dbip.parsing.DbIpIpToLocationAndIspCSVLineParser;
import technology.dice.dicewhere.reading.RawLine;

public class DbIpIpToLocationAndIspCSVLineParserTest {
  @Test
  public void ipV4LineWithOriginalLine() throws LineParsingException {
    DbIpIpToLocationAndIspCSVLineParser dbIpIpToLocationAndIspCSVLineParser =
        new DbIpIpToLocationAndIspCSVLineParser();
    String line =
        "1.0.0.0,1.0.0.255,AU,Queensland,Brisbane,\"South Brisbane\",4101,-27.4748,153.017,2207259,10,Australia/Brisbane,\"APNIC Research and Development\",,";
    RawLine rawLine = new RawLine(line, 1);

    Stream<ParsedLine> parsed =
        dbIpIpToLocationAndIspCSVLineParser.parse(new RawLine(line, 1), true);
    ParsedLine expected =
        new ParsedLine(
            new IP(InetAddresses.forString("1.0.0.0")),
            new IP(InetAddresses.forString("1.0.0.255")),
            IpInformation.builder()
                .withCountryCodeAlpha2("AU")
                .withCityGeonameId("2207259")
                .withCity("South Brisbane")
                .withLeastSpecificDivision("Queensland")
                .withMostSpecificDivision("Brisbane")
                .withPostcode("4101")
                .withStartOfRange(new IP(InetAddresses.forString("1.0.0.0")))
                .withEndOfRange(new IP(InetAddresses.forString("1.0.0.255")))
                .withOriginalLine(line)
                .build(),
            rawLine);
    assertEquals(expected, parsed.findFirst().get());
  }

  @Test
  public void ipV4LineWithoutOriginalLine() throws LineParsingException {
    DbIpIpToLocationAndIspCSVLineParser dbIpIpToLocationAndIspCSVLineParser =
        new DbIpIpToLocationAndIspCSVLineParser();
    String line =
        "1.0.0.0,1.0.0.255,AU,Queensland,Brisbane,\"South Brisbane\",4101,-27.4748,153.017,2207259,10,Australia/Brisbane,\"APNIC Research and Development\",,";
    RawLine rawLine = new RawLine(line, 1);
    Stream<ParsedLine> parsed =
        dbIpIpToLocationAndIspCSVLineParser.parse(new RawLine(line, 1), false);
    ParsedLine expected =
        new ParsedLine(
            new IP(InetAddresses.forString("1.0.0.0")),
            new IP(InetAddresses.forString("1.0.0.255")),
            IpInformation.builder()
                .withCountryCodeAlpha2("AU")
                .withCityGeonameId("2207259")
                .withCity("South Brisbane")
                .withLeastSpecificDivision("Queensland")
                .withMostSpecificDivision("Brisbane")
                .withPostcode("4101")
                .withStartOfRange(new IP(InetAddresses.forString("1.0.0.0")))
                .withEndOfRange(new IP(InetAddresses.forString("1.0.0.255")))
                .build(),
            rawLine);
    assertEquals(expected, parsed.findFirst().get());
  }

  @Test
  public void ipV6LineWithOriginal() throws LineParsingException {
    DbIpIpToLocationAndIspCSVLineParser dbIpIpToLocationAndIspCSVLineParser =
        new DbIpIpToLocationAndIspCSVLineParser();
    String line =
        "2c0f:fa41::,2c0f:fa47:ffff:ffff:ffff:ffff:ffff:ffff,MU,\"Plaines Wilhems\",,\"Ebene CyberCity\",,-20.2419,57.4896,1106748,4,Indian/Mauritius,\"African Network Information Center - ( AfriNIC Ltd )\",,";
    RawLine rawLine = new RawLine(line, 1);
    Stream<ParsedLine> parsed =
        dbIpIpToLocationAndIspCSVLineParser.parse(new RawLine(line, 1), true);
    ParsedLine expected =
        new ParsedLine(
            new IP(InetAddresses.forString("2c0f:fa41:0:0:0:0:0:0")),
            new IP(InetAddresses.forString("2c0f:fa47:ffff:ffff:ffff:ffff:ffff:ffff")),
            IpInformation.builder()
                .withCountryCodeAlpha2("MU")
                .withCityGeonameId("1106748")
                .withCity("Ebene CyberCity")
                .withLeastSpecificDivision("Plaines Wilhems")
                .withStartOfRange(new IP(InetAddresses.forString("2c0f:fa41:0:0:0:0:0:0")))
                .withEndOfRange(
                    new IP(InetAddresses.forString("2c0f:fa47:ffff:ffff:ffff:ffff:ffff:ffff")))
                .withOriginalLine(line)
                .build(),
            rawLine);
    assertEquals(expected, parsed.findFirst().get());
  }

  @Test
  public void ipV6LineWithWithoutOriginalLine() throws LineParsingException {
    DbIpIpToLocationAndIspCSVLineParser dbIpIpToLocationAndIspCSVLineParser =
        new DbIpIpToLocationAndIspCSVLineParser();
    String line =
        "2c0f:fa41::,2c0f:fa47:ffff:ffff:ffff:ffff:ffff:ffff,MU,\"Plaines Wilhems\",,\"Ebene CyberCity\",,-20.2419,57.4896,1106748,4,Indian/Mauritius,\"African Network Information Center - ( AfriNIC Ltd )\",,";
    RawLine rawLine = new RawLine(line, 1);
    Stream<ParsedLine> parsed =
        dbIpIpToLocationAndIspCSVLineParser.parse(new RawLine(line, 1), false);
    ParsedLine expected =
        new ParsedLine(
            new IP(InetAddresses.forString("2c0f:fa41:0:0:0:0:0:0")),
            new IP(InetAddresses.forString("2c0f:fa47:ffff:ffff:ffff:ffff:ffff:ffff")),
            IpInformation.builder()
                .withCountryCodeAlpha2("MU")
                .withCityGeonameId("1106748")
                .withCity("Ebene CyberCity")
                .withLeastSpecificDivision("Plaines Wilhems")
                .withStartOfRange(new IP(InetAddresses.forString("2c0f:fa41:0:0:0:0:0:0")))
                .withEndOfRange(
                    new IP(InetAddresses.forString("2c0f:fa47:ffff:ffff:ffff:ffff:ffff:ffff")))
                .build(),
            rawLine);
    assertEquals(expected, parsed.findFirst().get());
  }

  @Test
  public void wrongLineFormat() {
    assertThrows(
        LineParsingException.class,
        () -> {
          DbIpIpToLocationAndIspCSVLineParser dbIpIpToLocationAndIspCSVLineParser =
              new DbIpIpToLocationAndIspCSVLineParser();
          String line = "column1,column2,column3";
          try {
            dbIpIpToLocationAndIspCSVLineParser.parse(new RawLine(line, 1), false);
          } catch (LineParsingException e) {
            assertEquals(line, e.getOffendingLine().getLine());
            assertEquals(1, e.getOffendingLine().getLineNumber());
            throw e;
          }
        });
  }

  @Test
  public void wrongStartIpFormat() {
    assertThrows(
        LineParsingException.class,
        () -> {
          DbIpIpToLocationAndIspCSVLineParser dbIpIpToLocationAndIspCSVLineParser =
              new DbIpIpToLocationAndIspCSVLineParser();

          String line =
              "uh-Oh,1.0.0.255,AU,Queensland,Brisbane,\"South Brisbane\",4101,-27.4748,153.017,2207259,10,Australia/Brisbane,\"APNIC Research and Development\",,";
          try {
            dbIpIpToLocationAndIspCSVLineParser.parse(new RawLine(line, 1), false);
          } catch (LineParsingException e) {
            assertEquals(line, e.getOffendingLine().getLine());
            assertEquals(1, e.getOffendingLine().getLineNumber());
            throw e;
          }
        });
  }

  @Test
  public void wrongEndIpFormat() {
    assertThrows(
        LineParsingException.class,
        () -> {
          DbIpIpToLocationAndIspCSVLineParser dbIpIpToLocationAndIspCSVLineParser =
              new DbIpIpToLocationAndIspCSVLineParser();
          String line =
              "1.0.0.0,stop-Hammertime,AU,Queensland,Brisbane,\"South Brisbane\",4101,-27.4748,153.017,2207259,10,Australia/Brisbane,\"APNIC Research and Development\",,";
          try {
            dbIpIpToLocationAndIspCSVLineParser.parse(new RawLine(line, 1), false);
          } catch (LineParsingException e) {
            assertEquals(line, e.getOffendingLine().getLine());
            assertEquals(1, e.getOffendingLine().getLineNumber());
            throw e;
          }
        });
  }

  @Test
  public void onlyCountryIPV4() throws LineParsingException {
    DbIpIpToLocationAndIspCSVLineParser dbIpIpToLocationAndIspCSVLineParser =
        new DbIpIpToLocationAndIspCSVLineParser();
    String line = "\"1.4.128.0\",\"1.4.255.255\",\"TH\"";
    RawLine rawLine = new RawLine(line, 1);
    Stream<ParsedLine> parsed =
        dbIpIpToLocationAndIspCSVLineParser.parse(new RawLine(line, 1), false);
    ParsedLine expected =
        new ParsedLine(
            new IP(InetAddresses.forString("1.4.128.0")),
            new IP(InetAddresses.forString("1.4.255.255")),
            IpInformation.builder()
                .withCountryCodeAlpha2("TH")
                .withStartOfRange(new IP(InetAddresses.forString("1.4.128.0")))
                .withEndOfRange(new IP(InetAddresses.forString("1.4.255.255")))
                .build(),
            rawLine);
    assertEquals(expected, parsed.findFirst().get());
  }

  @Test
  public void onlyCountryIPV6() throws LineParsingException {
    DbIpIpToLocationAndIspCSVLineParser dbIpIpToLocationAndIspCSVLineParser =
        new DbIpIpToLocationAndIspCSVLineParser();
    String line = "\"2a0c:3800:400::\",\"2a0c:3800:400:ffff:ffff:ffff:ffff:ffff\",\"PT\"";
    RawLine rawLine = new RawLine(line, 1);
    Stream<ParsedLine> parsed =
        dbIpIpToLocationAndIspCSVLineParser.parse(new RawLine(line, 1), false);
    ParsedLine expected =
        new ParsedLine(
            new IP(InetAddresses.forString("2a0c:3800:400::")),
            new IP(InetAddresses.forString("2a0c:3800:400:ffff:ffff:ffff:ffff:ffff")),
            IpInformation.builder()
                .withCountryCodeAlpha2("PT")
                .withStartOfRange(new IP(InetAddresses.forString("2a0c:3800:400::")))
                .withEndOfRange(
                    new IP(InetAddresses.forString("2a0c:3800:400:ffff:ffff:ffff:ffff:ffff")))
                .build(),
            rawLine);
    assertEquals(expected, parsed.findFirst().get());
  }
}
