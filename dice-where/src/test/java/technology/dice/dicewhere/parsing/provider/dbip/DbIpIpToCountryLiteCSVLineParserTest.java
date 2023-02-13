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
import technology.dice.dicewhere.provider.dbip.parsing.DbIpIpToCountryLiteCSVLineParser;
import technology.dice.dicewhere.reading.RawLine;

public class DbIpIpToCountryLiteCSVLineParserTest {
  @Test
  public void ipV4LineWithOriginalLine() throws LineParsingException {
    DbIpIpToCountryLiteCSVLineParser dbIpIpToCountryLiteCSVLineParser =
        new DbIpIpToCountryLiteCSVLineParser();
    String line = "1.52.0.0,1.55.255.255,VN";
    RawLine rawLine = new RawLine(line, 1);
    Stream<ParsedLine> parsed = dbIpIpToCountryLiteCSVLineParser.parse(new RawLine(line, 1), true);
    ParsedLine expected =
        new ParsedLine(
            new IP(InetAddresses.forString("1.52.0.0")),
            new IP(InetAddresses.forString("1.55.255.255")),
            IpInformation.builder()
                .withCountryCodeAlpha2("VN")
                .withStartOfRange(new IP(InetAddresses.forString("1.52.0.0")))
                .withEndOfRange(new IP(InetAddresses.forString("1.55.255.255")))
                .withOriginalLine(line)
                .build(),
            rawLine);
    assertEquals(expected, parsed.findFirst().get());
  }

  @Test
  public void ipV4LineWithoutOriginalLine() throws LineParsingException {
    DbIpIpToCountryLiteCSVLineParser dbIpIpToCountryLiteCSVLineParser =
        new DbIpIpToCountryLiteCSVLineParser();
    String line = "1.52.0.0,1.55.255.255,VN";
    RawLine rawLine = new RawLine(line, 1);
    Stream<ParsedLine> parsed = dbIpIpToCountryLiteCSVLineParser.parse(new RawLine(line, 1), false);
    ParsedLine expected =
        new ParsedLine(
            new IP(InetAddresses.forString("1.52.0.0")),
            new IP(InetAddresses.forString("1.55.255.255")),
            IpInformation.builder()
                .withCountryCodeAlpha2("VN")
                .withStartOfRange(new IP(InetAddresses.forString("1.52.0.0")))
                .withEndOfRange(new IP(InetAddresses.forString("1.55.255.255")))
                .build(),
            rawLine);
    assertEquals(expected, parsed.findFirst().get());
  }

  @Test
  public void ipV6LineWithOriginal() throws LineParsingException {
    DbIpIpToCountryLiteCSVLineParser dbIpIpToCountryLiteCSVLineParser =
        new DbIpIpToCountryLiteCSVLineParser();
    String line = "2a0d:20c0::,2a0d:20c7:ffff:ffff:ffff:ffff:ffff:ffff,IT";
    RawLine rawLine = new RawLine(line, 1);
    Stream<ParsedLine> parsed = dbIpIpToCountryLiteCSVLineParser.parse(new RawLine(line, 1), true);
    ParsedLine expected =
        new ParsedLine(
            new IP(InetAddresses.forString("2a0d:20c0:0000:0000:0000:0000:0000:0000")),
            new IP(InetAddresses.forString("2a0d:20c7:ffff:ffff:ffff:ffff:ffff:ffff")),
            IpInformation.builder()
                .withCountryCodeAlpha2("IT")
                .withStartOfRange(
                    new IP(InetAddresses.forString("2a0d:20c0:0000:0000:0000:0000:0000:0000")))
                .withEndOfRange(
                    new IP(InetAddresses.forString("2a0d:20c7:ffff:ffff:ffff:ffff:ffff:ffff")))
                .withOriginalLine(line)
                .build(),
            rawLine);
    assertEquals(expected, parsed.findFirst().get());
  }

  @Test
  public void ipV6LineWithWithoutOriginalLine() throws LineParsingException {
    DbIpIpToCountryLiteCSVLineParser dbIpIpToCountryLiteCSVLineParser =
        new DbIpIpToCountryLiteCSVLineParser();
    String line = "2a0d:20c0::,2a0d:20c7:ffff:ffff:ffff:ffff:ffff:ffff,IT";
    RawLine rawLine = new RawLine(line, 1);
    Stream<ParsedLine> parsed = dbIpIpToCountryLiteCSVLineParser.parse(new RawLine(line, 1), false);
    ParsedLine expected =
        new ParsedLine(
            new IP(InetAddresses.forString("2a0d:20c0:0000:0000:0000:0000:0000:0000")),
            new IP(InetAddresses.forString("2a0d:20c7:ffff:ffff:ffff:ffff:ffff:ffff")),
            IpInformation.builder()
                .withCountryCodeAlpha2("IT")
                .withStartOfRange(
                    new IP(InetAddresses.forString("2a0d:20c0:0000:0000:0000:0000:0000:0000")))
                .withEndOfRange(
                    new IP(InetAddresses.forString("2a0d:20c7:ffff:ffff:ffff:ffff:ffff:ffff")))
                .build(),
            rawLine);
    assertEquals(expected, parsed.findFirst().get());
  }

  @Test
  public void wrongLineFormat() {
    assertThrows(
        LineParsingException.class,
        () -> {
          DbIpIpToCountryLiteCSVLineParser dbIpIpToCountryLiteCSVLineParser =
              new DbIpIpToCountryLiteCSVLineParser();
          String line = "column1";
          try {
            dbIpIpToCountryLiteCSVLineParser.parse(new RawLine(line, 1), false);
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
          DbIpIpToCountryLiteCSVLineParser dbIpIpToCountryLiteCSVLineParser =
              new DbIpIpToCountryLiteCSVLineParser();

          String line =
              "uh-Oh,1.0.0.255,AU,Queensland,Brisbane,\"South Brisbane\",4101,-27.4748,153.017,2207259,10,Australia/Brisbane,\"APNIC Research and Development\",,";
          try {
            dbIpIpToCountryLiteCSVLineParser.parse(new RawLine(line, 1), false);
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
          DbIpIpToCountryLiteCSVLineParser dbIpIpToCountryLiteCSVLineParser =
              new DbIpIpToCountryLiteCSVLineParser();
          String line =
              "1.0.0.0,stop-Hammertime,AU,Queensland,Brisbane,\"South Brisbane\",4101,-27.4748,153.017,2207259,10,Australia/Brisbane,\"APNIC Research and Development\",,";
          try {
            dbIpIpToCountryLiteCSVLineParser.parse(new RawLine(line, 1), false);
          } catch (LineParsingException e) {
            assertEquals(line, e.getOffendingLine().getLine());
            assertEquals(1, e.getOffendingLine().getLineNumber());
            throw e;
          }
        });
  }
}
