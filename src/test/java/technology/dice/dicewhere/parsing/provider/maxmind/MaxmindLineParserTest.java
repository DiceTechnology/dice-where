/*
 * Copyright (C) 2018 - present by Dice Technology Ltd.
 *
 * Please see distribution for license.
 */

package technology.dice.dicewhere.parsing.provider.maxmind;

import com.google.common.collect.ImmutableMap;
import com.google.common.net.InetAddresses;
import org.junit.*;
import technology.dice.dicewhere.api.api.IP;
import technology.dice.dicewhere.api.api.IpInformation;
import technology.dice.dicewhere.api.exceptions.LineParsingException;
import technology.dice.dicewhere.decorator.DecorationStrategy;
import technology.dice.dicewhere.decorator.DecoratorTestUtils;
import technology.dice.dicewhere.parsing.ParsedLine;
import technology.dice.dicewhere.provider.maxmind.parsing.MaxmindLineParser;
import technology.dice.dicewhere.provider.maxmind.reading.MaxmindLocation;
import technology.dice.dicewhere.reading.RawLine;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class MaxmindLineParserTest {

  private static Map<String, MaxmindLocation> locationNames;
  private MaxmindLineParser maxmindLineParser;

  @BeforeClass
  public static void beforeClass() {
    locationNames =
        ImmutableMap.<String, MaxmindLocation>builder()
            .put(
                "2634096", new MaxmindLocation("2634096", "GB", "Cumbria", "England", "Whitehaven"))
            .put("3372745", new MaxmindLocation("3372745", "PT", "", "Azores", "Rabo De Peixe"))
            .put("3372741", new MaxmindLocation("3372741", "BG", "", "Varna", "Varna"))
            .build();
  }

  @Before
  public void setUp() {
    maxmindLineParser = new MaxmindLineParser(locationNames);
  }

  @Test
  public void ipV4LineWithOriginalLine() throws LineParsingException {
    String line = "78.29.134.0/25,3372745,2264397,,0,0,9600-082,37.8000,-25.5833,500";
    RawLine rawLine = new RawLine(line, 1);
    ParsedLine expected =
        new ParsedLine(
            new IP(InetAddresses.forString("78.29.134.0")),
            new IP(InetAddresses.forString("78.29.134.127")),
            IpInformation.builder()
                .withCountryCodeAlpha2("PT")
                .withGeonameId("3372745")
                .withCity("Rabo De Peixe")
                .withLeastSpecificDivision("Azores")
                .withMostSpecificDivision("")
                .withPostcode("9600-082")
                .withStartOfRange(new IP(InetAddresses.forString("78.29.134.0")))
                .withEndOfRange(new IP(InetAddresses.forString("78.29.134.127")))
                .withOriginalLine(line)
                .build(),
            rawLine);

    Stream<ParsedLine> parsed = maxmindLineParser.parse(rawLine, true);

    assertEquals(expected, parsed.findFirst().get());
  }

  @Test
  public void ipV4LineWithoutOriginalLine() throws LineParsingException {
    String line = "78.29.134.0/25,3372745,2264397,,0,0,9600-082,37.8000,-25.5833,500";
    RawLine rawLine = new RawLine(line, 1);
    ParsedLine expected =
        new ParsedLine(
            new IP(InetAddresses.forString("78.29.134.0")),
            new IP(InetAddresses.forString("78.29.134.127")),
            IpInformation.builder()
                .withCountryCodeAlpha2("PT")
                .withGeonameId("3372745")
                .withCity("Rabo De Peixe")
                .withLeastSpecificDivision("Azores")
                .withMostSpecificDivision("")
                .withPostcode("9600-082")
                .withStartOfRange(new IP(InetAddresses.forString("78.29.134.0")))
                .withEndOfRange(new IP(InetAddresses.forString("78.29.134.127")))
                .build(),
            rawLine);

    Stream<ParsedLine> parsed = maxmindLineParser.parse(new RawLine(line, 1), false);

    assertEquals(expected, parsed.findFirst().get());
  }

  @Test
  @Ignore //DECORATION now happens prior to putting it in the DB
  public void shouldIdentifyIpv4RangesWithVpn_whenRangesDoNotOverlap()
      throws LineParsingException, IOException {
    MaxmindLineParser maxmindLineParser = new MaxmindLineParser(locationNames, DecoratorTestUtils.getMaxmindVpnDecorator(DecorationStrategy.ANY));

    String line = "1.0.2.0/24,3372745,2264397,,0,0,9600-082,37.8000,-25.5833,500";
    RawLine rawLine = new RawLine(line, 1);
    List<ParsedLine> parsed = maxmindLineParser.parse(rawLine, false).collect(Collectors.toList());
    IpInformation.Builder baseInfo =
        IpInformation.builder()
            .withCountryCodeAlpha2("PT")
            .withGeonameId("3372745")
            .withCity("Rabo De Peixe")
            .withLeastSpecificDivision("Azores")
            .withMostSpecificDivision("")
            .withPostcode("9600-082");
    List<ParsedLine> expected = new ArrayList<>();
    expected.add(
        new ParsedLine(
            new IP(InetAddresses.forString("1.0.2.0")),
            new IP(InetAddresses.forString("1.0.2.15")),
            baseInfo
                .isVpn(false)
                .withStartOfRange(new IP(InetAddresses.forString("1.0.2.0")))
                .withEndOfRange(new IP(InetAddresses.forString("1.0.2.15")))
                .build(),
            rawLine));
    expected.add(
        new ParsedLine(
            new IP(InetAddresses.forString("1.0.2.16")),
            new IP(InetAddresses.forString("1.0.2.31")),
            baseInfo
                .isVpn(true)
                .withStartOfRange(new IP(InetAddresses.forString("1.0.2.16")))
                .withEndOfRange(new IP(InetAddresses.forString("1.0.2.31")))
                .build(),
            rawLine));
    expected.add(
        new ParsedLine(
            new IP(InetAddresses.forString("1.0.2.32")),
            new IP(InetAddresses.forString("1.0.2.54")),
            baseInfo
                .isVpn(false)
                .withStartOfRange(new IP(InetAddresses.forString("1.0.2.32")))
                .withEndOfRange(new IP(InetAddresses.forString("1.0.2.54")))
                .build(),
            rawLine));
    expected.add(
        new ParsedLine(
            new IP(InetAddresses.forString("1.0.2.55")),
            new IP(InetAddresses.forString("1.0.2.55")),
            baseInfo
                .isVpn(true)
                .withStartOfRange(new IP(InetAddresses.forString("1.0.2.55")))
                .withEndOfRange(new IP(InetAddresses.forString("1.0.2.55")))
                .build(),
            rawLine));
    expected.add(
        new ParsedLine(
            new IP(InetAddresses.forString("1.0.2.56")),
            new IP(InetAddresses.forString("1.0.2.63")),
            baseInfo
                .isVpn(false)
                .withStartOfRange(new IP(InetAddresses.forString("1.0.2.56")))
                .withEndOfRange(new IP(InetAddresses.forString("1.0.2.63")))
                .build(),
            rawLine));
    expected.add(
            new ParsedLine(
                    new IP(InetAddresses.forString("1.0.2.64")),
                    new IP(InetAddresses.forString("1.0.2.79")),
                    baseInfo
                            .isVpn(true)
                            .withStartOfRange(new IP(InetAddresses.forString("1.0.2.64")))
                            .withEndOfRange(new IP(InetAddresses.forString("1.0.2.79")))
                            .build(),
                    rawLine));
    expected.add(
            new ParsedLine(
                    new IP(InetAddresses.forString("1.0.2.80")),
                    new IP(InetAddresses.forString("1.0.2.255")),
                    baseInfo
                            .isVpn(false)
                            .withStartOfRange(new IP(InetAddresses.forString("1.0.2.80")))
                            .withEndOfRange(new IP(InetAddresses.forString("1.0.2.255")))
                            .build(),
                    rawLine));
    Assert.assertEquals(expected, parsed);
  }

  @Test
  public void ipV6LineWithOriginal() throws LineParsingException {
    String line = "2a02:c7f:6a02::/47,2634096,2635167,,0,0,CA28,54.5578,-3.5837,10";
    RawLine rawLine = new RawLine(line, 1);
    ParsedLine expected =
        new ParsedLine(
            new IP(InetAddresses.forString("2a02:c7f:6a02:0:0:0:0:0")),
            new IP(InetAddresses.forString("2a02:c7f:6a03:ffff:ffff:ffff:ffff:ffff")),
            IpInformation.builder()
                .withCountryCodeAlpha2("GB")
                .withGeonameId("2634096")
                .withCity("Whitehaven")
                .withLeastSpecificDivision("England")
                .withMostSpecificDivision("Cumbria")
                .withPostcode("CA28")
                .withStartOfRange(new IP(InetAddresses.forString("2a02:c7f:6a02:0:0:0:0:0")))
                .withEndOfRange(
                    new IP(InetAddresses.forString("2a02:c7f:6a03:ffff:ffff:ffff:ffff:ffff")))
                .withOriginalLine(line)
                .build(),
            rawLine);

    Stream<ParsedLine> parsed = maxmindLineParser.parse(new RawLine(line, 1), true);

    assertEquals(expected, parsed.findFirst().get());
  }

  @Test
  public void locationMissing() throws LineParsingException {
    String line = "::/0,5,5,,0,0,CA28,54.5578,-3.5837,10";
    RawLine rawLine = new RawLine(line, 1);
    ParsedLine expected =
        new ParsedLine(
            new IP(InetAddresses.forString("0:0:0:0:0:0:0:0")),
            new IP(InetAddresses.forString("ffff:ffff:ffff:ffff:ffff:ffff:ffff:ffff")),
            IpInformation.builder()
                .withCountryCodeAlpha2("ZZ")
                .withGeonameId("5")
                .withCity("")
                .withLeastSpecificDivision("")
                .withMostSpecificDivision("")
                .withPostcode("CA28")
                .withStartOfRange(new IP(InetAddresses.forString("0:0:0:0:0:0:0:0")))
                .withEndOfRange(
                    new IP(InetAddresses.forString("ffff:ffff:ffff:ffff:ffff:ffff:ffff:ffff")))
                .withOriginalLine(line)
                .build(),
            rawLine);

    Stream<ParsedLine> parsed = maxmindLineParser.parse(new RawLine(line, 1), true);

    assertEquals(expected, parsed.findFirst().get());
  }

  @Test
  public void ipV6LineWithoutOriginal() throws LineParsingException {
    String line = "2a02:c7f:6a02::/47,2634096,2635167,,0,0,CA28,54.5578,-3.5837,10";
    RawLine rawLine = new RawLine(line, 1);
    ParsedLine expected =
        new ParsedLine(
            new IP(InetAddresses.forString("2a02:c7f:6a02:0:0:0:0:0")),
            new IP(InetAddresses.forString("2a02:c7f:6a03:ffff:ffff:ffff:ffff:ffff")),
            IpInformation.builder()
                .withCountryCodeAlpha2("GB")
                .withGeonameId("2634096")
                .withCity("Whitehaven")
                .withLeastSpecificDivision("England")
                .withMostSpecificDivision("Cumbria")
                .withPostcode("CA28")
                .withStartOfRange(new IP(InetAddresses.forString("2a02:c7f:6a02:0:0:0:0:0")))
                .withEndOfRange(
                    new IP(InetAddresses.forString("2a02:c7f:6a03:ffff:ffff:ffff:ffff:ffff")))
                .build(),
            rawLine);

    Stream<ParsedLine> parsed = maxmindLineParser.parse(new RawLine(line, 1), false);

    assertEquals(expected, parsed.findFirst().get());
  }

  @Test
  public void wrongLineFormat() {
    String line = "column1,column2,column3";
    try {
      maxmindLineParser.parse(new RawLine(line, 1), false);
      fail("Expected LineParsingException which was never thrown");
    } catch (LineParsingException e) {
      assertEquals(line, e.getOffendingLine().getLine());
      assertEquals(1, e.getOffendingLine().getLineNumber());
    }
  }

  @Test
  public void wrongCIDRFormat() {
    String line = "2a02:c7f:6a02:Nop:/47,2634096,2635167,,0,0,CA28,54.5578,-3.5837,10";
    try {
      maxmindLineParser.parse(new RawLine(line, 1), false);
      fail("Expected LineParsingException which was never thrown");
    } catch (LineParsingException e) {
      assertEquals(line, e.getOffendingLine().getLine());
      assertEquals(1, e.getOffendingLine().getLineNumber());
    }
  }

  @Test
  public void impossibleCIDR() {
    String line = "2a02:c7f:6a02::/1000,2634096,2635167,,0,0,CA28,54.5578,-3.5837,10";
    try {
      maxmindLineParser.parse(new RawLine(line, 1), false);
      fail("Expected LineParsingException which was never thrown");
    } catch (LineParsingException e) {
      assertEquals(line, e.getOffendingLine().getLine());
      assertEquals(1, e.getOffendingLine().getLineNumber());
    }
  }
}
