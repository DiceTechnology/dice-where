/*
 * Copyright (C) 2018 - present by Dice Technology Ltd.
 *
 * Please see distribution for license.
 */

package technology.dice.dicewhere.parsing.provider.maxmind;

import com.google.common.collect.ImmutableMap;
import com.google.common.net.InetAddresses;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import technology.dice.dicewhere.api.api.IP;
import technology.dice.dicewhere.api.api.IpInformation;
import technology.dice.dicewhere.api.exceptions.LineParsingException;
import technology.dice.dicewhere.parsing.ParsedLine;
import technology.dice.dicewhere.provider.maxmind.parsing.MaxmindLineParser;
import technology.dice.dicewhere.provider.maxmind.reading.MaxmindAnonymous;
import technology.dice.dicewhere.provider.maxmind.reading.MaxmindAnonymousDbParser;
import technology.dice.dicewhere.provider.maxmind.reading.MaxmindLocation;
import technology.dice.dicewhere.reading.RawLine;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MaxmindLineParserTest {

  private static Map<String, MaxmindLocation> locationNames;
  private String IPV4_ANONYMOUS_LINES =
      "network,is_anonymous,is_anonymous_vpn,is_hosting_provider,is_public_proxy,is_tor_exit_node\n"
          + "1.0.2.16/28,1,1,0,0,0\n"
          + "1.0.2.32/28,1,0,0,0,0\n"
          + "1.0.2.64/28,1,1,0,0,0\n"
          + "1.0.3.0/24,1,1,0,0,0\n";

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

  @Test
  public void ipV4LineWithOriginalLine() throws LineParsingException {
    MaxmindLineParser maxmindLineParser = new MaxmindLineParser(locationNames);
    String line = "78.29.134.0/25,3372745,2264397,,0,0,9600-082,37.8000,-25.5833,500";
    RawLine rawLine = new RawLine(line, 1);
    Stream<ParsedLine> parsed = maxmindLineParser.parse(rawLine, true);
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
    Assert.assertEquals(expected, parsed.findFirst().get());
  }

  @Test
  public void ipV4LineWithoutOriginalLine() throws LineParsingException {
    MaxmindLineParser maxmindLineParser = new MaxmindLineParser(locationNames);
    String line = "78.29.134.0/25,3372745,2264397,,0,0,9600-082,37.8000,-25.5833,500";
    RawLine rawLine = new RawLine(line, 1);
    Stream<ParsedLine> parsed = maxmindLineParser.parse(new RawLine(line, 1), false);
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
    Assert.assertEquals(expected, parsed.findFirst().get());
  }

  @Test
  public void shouldIdentifyIpv4RangesWithVpn_whenRangesDoNotOverlap()
      throws LineParsingException, IOException {
    String ipv6Lines =
        "network,is_anonymous,is_anonymous_vpn,is_hosting_provider,is_public_proxy,is_tor_exit_node";
    InputStream streamV4 = new ByteArrayInputStream(IPV4_ANONYMOUS_LINES.getBytes());
    BufferedReader bufferedReaderV4 = new BufferedReader(new InputStreamReader(streamV4));
    InputStream streamV6 = new ByteArrayInputStream(ipv6Lines.getBytes());
    BufferedReader bufferedReaderV6 = new BufferedReader(new InputStreamReader(streamV6));
    MaxmindAnonymousDbParser anonymousDbParser =
        new MaxmindAnonymousDbParser(bufferedReaderV4, bufferedReaderV6, MaxmindAnonymous::isVpn);
    MaxmindLineParser maxmindLineParser = new MaxmindLineParser(locationNames, anonymousDbParser);

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
            new IP(InetAddresses.forString("1.0.2.63")),
            baseInfo
                .isVpn(false)
                .withStartOfRange(new IP(InetAddresses.forString("1.0.2.32")))
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
  public void shouldIdentifyIpv4RangesWithVpn_whenRangesOverlap()
      throws LineParsingException, IOException {

    String ipv6Lines =
        "network,is_anonymous,is_anonymous_vpn,is_hosting_provider,is_public_proxy,is_tor_exit_node";
    InputStream streamV4 = new ByteArrayInputStream(IPV4_ANONYMOUS_LINES.getBytes());
    BufferedReader bufferedReaderV4 = new BufferedReader(new InputStreamReader(streamV4));
    InputStream streamV6 = new ByteArrayInputStream(ipv6Lines.getBytes());
    BufferedReader bufferedReaderV6 = new BufferedReader(new InputStreamReader(streamV6));
    MaxmindAnonymousDbParser anonymousDbParser =
        new MaxmindAnonymousDbParser(bufferedReaderV4, bufferedReaderV6, MaxmindAnonymous::isVpn);
    MaxmindLineParser maxmindLineParser = new MaxmindLineParser(locationNames, anonymousDbParser);

    String line1 = "1.0.3.0/25,3372741,2264397,,0,0,9600-082,37.8000,-25.5833,500";
    String line2 = "1.0.3.128/25,3372741,2264397,,0,0,9600-082,37.8000,-25.5833,500";
    RawLine rawLine1 = new RawLine(line1, 1);
    RawLine rawLine2 = new RawLine(line2, 2);
    List<ParsedLine> parsed = maxmindLineParser.parse(rawLine1, false).collect(Collectors.toList());
    parsed.addAll(maxmindLineParser.parse(rawLine2, false).collect(Collectors.toList()));
    IpInformation.Builder baseInfo =
        IpInformation.builder()
            .withCountryCodeAlpha2("BG")
            .withGeonameId("3372741")
            .withCity("Varna")
            .withLeastSpecificDivision("Varna")
            .withPostcode("9600-082");
    List<ParsedLine> expected = new ArrayList<>();
    expected.add(
        new ParsedLine(
            new IP(InetAddresses.forString("1.0.3.0")),
            new IP(InetAddresses.forString("1.0.3.127")),
            baseInfo
                .isVpn(true)
                .withStartOfRange(new IP(InetAddresses.forString("1.0.3.0")))
                .withEndOfRange(new IP(InetAddresses.forString("1.0.3.127")))
                .build(),
            rawLine1));
    expected.add(
        new ParsedLine(
            new IP(InetAddresses.forString("1.0.3.128")),
            new IP(InetAddresses.forString("1.0.3.255")),
            baseInfo
                .isVpn(true)
                .withStartOfRange(new IP(InetAddresses.forString("1.0.3.128")))
                .withEndOfRange(new IP(InetAddresses.forString("1.0.3.255")))
                .build(),
            rawLine2));
    Assert.assertEquals(expected, parsed);
  }

  @Test
  public void ipV6LineWithOriginal() throws LineParsingException {
    MaxmindLineParser maxmindParser = new MaxmindLineParser(locationNames);
    String line = "2a02:c7f:6a02::/47,2634096,2635167,,0,0,CA28,54.5578,-3.5837,10";
    RawLine rawLine = new RawLine(line, 1);
    Stream<ParsedLine> parsed = maxmindParser.parse(new RawLine(line, 1), true);
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
    Assert.assertEquals(expected, parsed.findFirst().get());
  }

  @Test
  public void locationMissing() throws LineParsingException {
    MaxmindLineParser maxmindParser = new MaxmindLineParser(locationNames);
    String line = "::/0,5,5,,0,0,CA28,54.5578,-3.5837,10";
    RawLine rawLine = new RawLine(line, 1);
    Stream<ParsedLine> parsed = maxmindParser.parse(new RawLine(line, 1), true);
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
    Assert.assertEquals(expected, parsed.findFirst().get());
  }

  @Test
  public void ipV6LineWithoutOriginal() throws LineParsingException {
    MaxmindLineParser maxmindParser = new MaxmindLineParser(locationNames);
    String line = "2a02:c7f:6a02::/47,2634096,2635167,,0,0,CA28,54.5578,-3.5837,10";
    RawLine rawLine = new RawLine(line, 1);
    Stream<ParsedLine> parsed = maxmindParser.parse(new RawLine(line, 1), false);
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
    Assert.assertEquals(expected, parsed.findFirst().get());
  }

  @Test(expected = LineParsingException.class)
  public void wrongLineFormat() throws LineParsingException {
    MaxmindLineParser maxmindParser = new MaxmindLineParser(locationNames);
    String line = "column1,column2,column3";
    try {
      maxmindParser.parse(new RawLine(line, 1), false);
    } catch (LineParsingException e) {
      Assert.assertEquals(line, e.getOffendingLine().getLine());
      Assert.assertEquals(1, e.getOffendingLine().getLineNumber());
      throw e;
    }
  }

  @Test(expected = LineParsingException.class)
  public void wrongCIDRFormat() throws LineParsingException {
    MaxmindLineParser maxmindParser = new MaxmindLineParser(locationNames);
    String line = "2a02:c7f:6a02:Nop:/47,2634096,2635167,,0,0,CA28,54.5578,-3.5837,10";
    try {
      maxmindParser.parse(new RawLine(line, 1), false);
    } catch (LineParsingException e) {
      Assert.assertEquals(line, e.getOffendingLine().getLine());
      Assert.assertEquals(1, e.getOffendingLine().getLineNumber());
      throw e;
    }
  }

  @Test(expected = LineParsingException.class)
  public void impossibleCIDR() throws LineParsingException {
    MaxmindLineParser maxmindParser = new MaxmindLineParser(locationNames);
    String line = "2a02:c7f:6a02::/1000,2634096,2635167,,0,0,CA28,54.5578,-3.5837,10";
    try {
      maxmindParser.parse(new RawLine(line, 1), false);
    } catch (LineParsingException e) {
      Assert.assertEquals(line, e.getOffendingLine().getLine());
      Assert.assertEquals(1, e.getOffendingLine().getLineNumber());
      throw e;
    }
  }
}
