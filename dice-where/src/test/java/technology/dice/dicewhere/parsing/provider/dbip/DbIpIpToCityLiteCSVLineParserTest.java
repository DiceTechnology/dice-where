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
import technology.dice.dicewhere.provider.dbip.parsing.DbIpIpToCityLiteCSVLineParser;
import technology.dice.dicewhere.reading.RawLine;

public class DbIpIpToCityLiteCSVLineParserTest {
  @Test
  public void ipV4LineWithOriginalLine() throws LineParsingException {
    DbIpIpToCityLiteCSVLineParser dbIpIpToCityLiteCSVLineParser =
        new DbIpIpToCityLiteCSVLineParser();
    String line = "1.8.154.0,1.8.155.255,AS,CN,Beijing,Beijing,39.904,116.408";
    RawLine rawLine = new RawLine(line, 1);
    Stream<ParsedLine> parsed = dbIpIpToCityLiteCSVLineParser.parse(new RawLine(line, 1), true);
    ParsedLine expected =
        new ParsedLine(
            new IP(InetAddresses.forString("1.8.154.0")),
            new IP(InetAddresses.forString("1.8.155.255")),
            IpInformation.builder()
                .withCountryCodeAlpha2("CN")
                .withCity("Beijing")
                .withLeastSpecificDivision("Beijing")
                .withStartOfRange(new IP(InetAddresses.forString("1.8.154.0")))
                .withEndOfRange(new IP(InetAddresses.forString("1.8.155.255")))
                .withOriginalLine(line)
                .build(),
            rawLine);
    assertEquals(expected, parsed.findFirst().get());
  }

  @Test
  public void ipV4LineWithoutOriginalLine() throws LineParsingException {
    DbIpIpToCityLiteCSVLineParser dbIpIpToCityLiteCSVLineParser =
        new DbIpIpToCityLiteCSVLineParser();
    String line = "1.8.154.0,1.8.155.255,AS,CN,Beijing,Beijing,39.904,116.408";
    RawLine rawLine = new RawLine(line, 1);
    Stream<ParsedLine> parsed = dbIpIpToCityLiteCSVLineParser.parse(new RawLine(line, 1), false);
    ParsedLine expected =
        new ParsedLine(
            new IP(InetAddresses.forString("1.8.154.0")),
            new IP(InetAddresses.forString("1.8.155.255")),
            IpInformation.builder()
                .withCountryCodeAlpha2("CN")
                .withCity("Beijing")
                .withLeastSpecificDivision("Beijing")
                .withStartOfRange(new IP(InetAddresses.forString("1.8.154.0")))
                .withEndOfRange(new IP(InetAddresses.forString("1.8.155.255")))
                .build(),
            rawLine);
    assertEquals(expected, parsed.findFirst().get());
  }

  @Test
  public void ipV6LineWithOriginal() throws LineParsingException {
    DbIpIpToCityLiteCSVLineParser dbIpIpToCityLiteCSVLineParser =
        new DbIpIpToCityLiteCSVLineParser();
    String line =
        "2c0f:ff80::,2c0f:ff80:ffff:ffff:ffff:ffff:ffff:ffff,AF,ZA,Gauteng,\"Sandton (Woodmead)\",-26.0561,28.0696";
    RawLine rawLine = new RawLine(line, 1);
    Stream<ParsedLine> parsed = dbIpIpToCityLiteCSVLineParser.parse(new RawLine(line, 1), true);
    ParsedLine expected =
        new ParsedLine(
            new IP(InetAddresses.forString("2C0F:FF80:0000:0000:0000:0000:0000:0000")),
            new IP(InetAddresses.forString("2c0f:ff80:ffff:ffff:ffff:ffff:ffff:ffff")),
            IpInformation.builder()
                .withCountryCodeAlpha2("ZA")
                .withCity("Sandton (Woodmead)")
                .withLeastSpecificDivision("Gauteng")
                .withStartOfRange(
                    new IP(InetAddresses.forString("2C0F:FF80:0000:0000:0000:0000:0000:0000")))
                .withEndOfRange(
                    new IP(InetAddresses.forString("2c0f:ff80:ffff:ffff:ffff:ffff:ffff:ffff")))
                .withOriginalLine(line)
                .build(),
            rawLine);
    assertEquals(expected, parsed.findFirst().get());
  }

  @Test
  public void ipV6LineWithWithoutOriginalLine() throws LineParsingException {
    DbIpIpToCityLiteCSVLineParser dbIpIpToCityLiteCSVLineParser =
        new DbIpIpToCityLiteCSVLineParser();
    String line =
        "2c0f:ff80::,2c0f:ff80:ffff:ffff:ffff:ffff:ffff:ffff,AF,ZA,Gauteng,\"Sandton (Woodmead)\",-26.0561,28.0696";
    RawLine rawLine = new RawLine(line, 1);
    Stream<ParsedLine> parsed = dbIpIpToCityLiteCSVLineParser.parse(new RawLine(line, 1), false);
    ParsedLine expected =
        new ParsedLine(
            new IP(InetAddresses.forString("2C0F:FF80:0000:0000:0000:0000:0000:0000")),
            new IP(InetAddresses.forString("2c0f:ff80:ffff:ffff:ffff:ffff:ffff:ffff")),
            IpInformation.builder()
                .withCountryCodeAlpha2("ZA")
                .withCity("Sandton (Woodmead)")
                .withLeastSpecificDivision("Gauteng")
                .withStartOfRange(
                    new IP(InetAddresses.forString("2C0F:FF80:0000:0000:0000:0000:0000:0000")))
                .withEndOfRange(
                    new IP(InetAddresses.forString("2c0f:ff80:ffff:ffff:ffff:ffff:ffff:ffff")))
                .build(),
            rawLine);
    assertEquals(expected, parsed.findFirst().get());
  }

  @Test()
  public void wrongLineFormat() {
    assertThrows(
        LineParsingException.class,
        () -> {
          DbIpIpToCityLiteCSVLineParser dbIpIpToCityLiteCSVLineParser =
              new DbIpIpToCityLiteCSVLineParser();
          String line = "column1,column2,column3";
          try {
            dbIpIpToCityLiteCSVLineParser.parse(new RawLine(line, 1), false);
          } catch (LineParsingException e) {
            assertEquals(line, e.getOffendingLine().getLine());
            assertEquals(1, e.getOffendingLine().getLineNumber());
            throw e;
          }
        });
  }

  @Test()
  public void wrongStartIpFormat() {
    assertThrows(
        LineParsingException.class,
        () -> {
          DbIpIpToCityLiteCSVLineParser dbIpIpToCityLiteCSVLineParser =
              new DbIpIpToCityLiteCSVLineParser();

          String line =
              "uh-Oh,1.0.0.255,AU,Queensland,Brisbane,\"South Brisbane\",4101,-27.4748,153.017,2207259,10,Australia/Brisbane,\"APNIC Research and Development\",,";
          try {
            dbIpIpToCityLiteCSVLineParser.parse(new RawLine(line, 1), false);
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
          DbIpIpToCityLiteCSVLineParser dbIpIpToCityLiteCSVLineParser =
              new DbIpIpToCityLiteCSVLineParser();
          String line =
              "1.0.0.0,stop-Hammertime,AU,Queensland,Brisbane,\"South Brisbane\",4101,-27.4748,153.017,2207259,10,Australia/Brisbane,\"APNIC Research and Development\",,";
          try {
            dbIpIpToCityLiteCSVLineParser.parse(new RawLine(line, 1), false);
          } catch (LineParsingException e) {
            assertEquals(line, e.getOffendingLine().getLine());
            assertEquals(1, e.getOffendingLine().getLineNumber());
            throw e;
          }
        });
  }
}
