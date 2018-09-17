/*
 * Copyright (C) 2018 - present by Dice Technology Ltd.
 *
 * Please see distribution for license.
 */

package technology.dice.dicewhere.parsing.provider.maxmind;

import inet.ipaddr.IPAddressString;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import technology.dice.dicewhere.provider.maxmind.reading.MaxmindAnonymous;
import technology.dice.dicewhere.provider.maxmind.reading.MaxmindAnonymousDbParser;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class MaxmindAnonymousDbParserTest {

  @Test
  public void shouldParseIPv4_unfiltered() throws IOException {
    String ipv4Lines =
        "network,is_anonymous,is_anonymous_vpn,is_hosting_provider,is_public_proxy,is_tor_exit_node\n"
            + "1.0.1.16/28,1,1,0,0,0\n"
            + "1.0.2.16/28,1,1,0,0,0\n"
            + "1.0.2.32/28,1,0,0,0,0\n"
            + "1.0.2.55/32,1,1,0,0,0\n"
            + "1.0.2.64/28,1,1,0,0,0\n"
            + "1.0.3.16/28,1,1,0,0,0\n"
            + "1.0.3.32/28,1,0,0,0,0\n"
            + "1.0.3.64/28,1,1,0,0,0";
    String ipv6Lines =
        "network,is_anonymous,is_anonymous_vpn,is_hosting_provider,is_public_proxy,is_tor_exit_node";
    InputStream streamV4 = new ByteArrayInputStream(ipv4Lines.getBytes());
    BufferedReader bufferedReaderV4 = new BufferedReader(new InputStreamReader(streamV4));
    InputStream streamV6 = new ByteArrayInputStream(ipv6Lines.getBytes());
    BufferedReader bufferedReaderV6 = new BufferedReader(new InputStreamReader(streamV6));
    MaxmindAnonymousDbParser parser =
        new MaxmindAnonymousDbParser(bufferedReaderV4, bufferedReaderV6);
    List<MaxmindAnonymous> parsedLines =
        parser
            .fetchForRange(new IPAddressString("1.0.2.0/24").getAddress())
            .collect(Collectors.toList());

    List<MaxmindAnonymous> expected = new ArrayList<>();
    expected.add(
        MaxmindAnonymous.builder(new IPAddressString("1.0.2.16/28").getAddress())
            .isAnonymous(true)
            .isVpn(true)
            .build());
    expected.add(
        MaxmindAnonymous.builder(new IPAddressString("1.0.2.32/28").getAddress())
            .isAnonymous(true)
            .build());
    expected.add(
            MaxmindAnonymous.builder(new IPAddressString("1.0.2.55/32").getAddress())
                    .isAnonymous(true)
                    .isVpn(true)
                    .build());
    expected.add(
        MaxmindAnonymous.builder(new IPAddressString("1.0.2.64/28").getAddress())
            .isAnonymous(true)
            .isVpn(true)
            .build());

    Assert.assertEquals(expected, parsedLines);
  }

  @Test
  public void shouldParseIPv4_returnNoneBecauseListIsNotOrdered() throws IOException {
    String ipv4Lines =
        "network,is_anonymous,is_anonymous_vpn,is_hosting_provider,is_public_proxy,is_tor_exit_node\n"
            + "1.0.3.64/28,1,1,0,0,0\n"
            + "1.0.2.16/28,1,1,0,0,0\n"
            + "1.0.2.32/28,1,0,0,0,0\n"
            + "1.0.2.64/28,1,1,0,0,0\n"
            + "1.0.3.16/28,1,1,0,0,0\n"
            + "1.0.3.32/28,1,0,0,0,0\n"
            + "1.0.3.64/28,1,1,0,0,0";
    String ipv6Lines =
        "network,is_anonymous,is_anonymous_vpn,is_hosting_provider,is_public_proxy,is_tor_exit_node";
    InputStream streamV4 = new ByteArrayInputStream(ipv4Lines.getBytes());
    BufferedReader bufferedReaderV4 = new BufferedReader(new InputStreamReader(streamV4));
    InputStream streamV6 = new ByteArrayInputStream(ipv6Lines.getBytes());
    BufferedReader bufferedReaderV6 = new BufferedReader(new InputStreamReader(streamV6));
    MaxmindAnonymousDbParser parser =
        new MaxmindAnonymousDbParser(bufferedReaderV4, bufferedReaderV6);
    List<MaxmindAnonymous> parsedLines =
        parser
            .fetchForRange(new IPAddressString("1.0.2.0/24").getAddress())
            .collect(Collectors.toList());

    Assert.assertTrue(parsedLines.isEmpty());
  }

  @Test
  public void shouldParseIPv4_filteredByVpn() throws IOException {
    String ipv4Lines =
        "network,is_anonymous,is_anonymous_vpn,is_hosting_provider,is_public_proxy,is_tor_exit_node\n"
            + "1.0.2.16/28,1,1,0,0,0\n"
            + "1.0.2.32/28,1,0,0,0,0\n"
            + "1.0.2.64/28,1,1,0,0,0\n"
            + "1.0.3.16/28,1,1,0,0,0\n"
            + "1.0.3.32/28,1,0,0,0,0\n"
            + "1.0.3.64/28,1,1,0,0,0";
    String ipv6Lines =
        "network,is_anonymous,is_anonymous_vpn,is_hosting_provider,is_public_proxy,is_tor_exit_node";
    InputStream streamV4 = new ByteArrayInputStream(ipv4Lines.getBytes());
    BufferedReader bufferedReaderV4 = new BufferedReader(new InputStreamReader(streamV4));
    InputStream streamV6 = new ByteArrayInputStream(ipv6Lines.getBytes());
    BufferedReader bufferedReaderV6 = new BufferedReader(new InputStreamReader(streamV6));
    MaxmindAnonymousDbParser parser =
        new MaxmindAnonymousDbParser(bufferedReaderV4, bufferedReaderV6, MaxmindAnonymous::isVpn);
    List<MaxmindAnonymous> parsedLines =
        parser
            .fetchForRange(new IPAddressString("1.0.2.0/23").getAddress())
            .collect(Collectors.toList());

    List<MaxmindAnonymous> expected = new ArrayList<>();
    expected.add(
        MaxmindAnonymous.builder(new IPAddressString("1.0.2.16/28").getAddress())
            .isAnonymous(true)
            .isVpn(true)
            .build());
    expected.add(
        MaxmindAnonymous.builder(new IPAddressString("1.0.2.64/28").getAddress())
            .isAnonymous(true)
            .isVpn(true)
            .build());
    expected.add(
        MaxmindAnonymous.builder(new IPAddressString("1.0.3.16/28").getAddress())
            .isAnonymous(true)
            .isVpn(true)
            .build());
    expected.add(
        MaxmindAnonymous.builder(new IPAddressString("1.0.3.64/28").getAddress())
            .isAnonymous(true)
            .isVpn(true)
            .build());

    Assert.assertEquals(expected, parsedLines);
  }

  @Test
  @Ignore
  public void shouldParseIPv6_filteredByVpn() throws IOException {
    String ipv4Lines =
            "network,is_anonymous,is_anonymous_vpn,is_hosting_provider,is_public_proxy,is_tor_exit_node\n"
                    + "1.0.2.16/28,1,1,0,0,0\n"
                    + "1.0.2.32/28,1,0,0,0,0\n"
                    + "1.0.2.64/28,1,1,0,0,0\n"
                    + "1.0.3.16/28,1,1,0,0,0\n"
                    + "1.0.3.32/28,1,0,0,0,0\n"
                    + "1.0.3.64/28,1,1,0,0,0";
    String ipv6Lines =
            "network,is_anonymous,is_anonymous_vpn,is_hosting_provider,is_public_proxy,is_tor_exit_node\n"
                    + "2001:470:7:800::/55,1,1,0,0,0\n"
                    + "2001:470:7:a00::/56,1,1,0,0,0\n"
                    + "2001:470:7:b70::/62,1,1,0,0,0\n"
                    + "2001:470:8:80::/59,1,1,0,0,0\n";
    InputStream streamV4 = new ByteArrayInputStream(ipv4Lines.getBytes());
    BufferedReader bufferedReaderV4 = new BufferedReader(new InputStreamReader(streamV4));
    InputStream streamV6 = new ByteArrayInputStream(ipv6Lines.getBytes());
    BufferedReader bufferedReaderV6 = new BufferedReader(new InputStreamReader(streamV6));
    MaxmindAnonymousDbParser parser =
            new MaxmindAnonymousDbParser(bufferedReaderV4, bufferedReaderV6, MaxmindAnonymous::isVpn);
    List<MaxmindAnonymous> parsedLines =
            parser
                    .fetchForRange(new IPAddressString("2001:470:7:a00::/53").getAddress())
                    .collect(Collectors.toList());

    List<MaxmindAnonymous> expected = new ArrayList<>();
    expected.add(
            MaxmindAnonymous.builder(new IPAddressString("2001:470:7:a00::/56").getAddress())
                    .isAnonymous(true)
                    .isVpn(true)
                    .build());
    expected.add(
            MaxmindAnonymous.builder(new IPAddressString("2001:470:7:b70::/62").getAddress())
                    .isAnonymous(true)
                    .isVpn(true)
                    .build());

    Assert.assertEquals(expected.get(0), parsedLines.get(0));
    Assert.assertEquals(expected.get(1), parsedLines.get(1));
  }

  @Test
  public void shouldParseIPv4_filteredByVpnOverlappingRanges() throws IOException {
    String ipv4Lines =
        "network,is_anonymous,is_anonymous_vpn,is_hosting_provider,is_public_proxy,is_tor_exit_node\n"
            + "1.0.1.0/28,1,1,0,0,0\n"
            + "1.0.1.16/28,1,1,0,0,0\n"
            + "1.0.1.64/28,1,1,0,0,0\n"
            + "1.0.2.0/24,1,1,0,0,0\n"
            + "1.0.3.16/28,1,1,0,0,0";
    String ipv6Lines =
        "network,is_anonymous,is_anonymous_vpn,is_hosting_provider,is_public_proxy,is_tor_exit_node";
    InputStream streamV4 = new ByteArrayInputStream(ipv4Lines.getBytes());
    BufferedReader bufferedReaderV4 = new BufferedReader(new InputStreamReader(streamV4));
    InputStream streamV6 = new ByteArrayInputStream(ipv6Lines.getBytes());
    BufferedReader bufferedReaderV6 = new BufferedReader(new InputStreamReader(streamV6));
    MaxmindAnonymousDbParser parser =
        new MaxmindAnonymousDbParser(bufferedReaderV4, bufferedReaderV6, MaxmindAnonymous::isVpn);
    List<MaxmindAnonymous> parsedLines =
        parser
            .fetchForRange(new IPAddressString("1.0.2.0/25").getAddress())
            .collect(Collectors.toList());
    parsedLines.addAll(
        parser
            .fetchForRange(new IPAddressString("1.0.2.128/25").getAddress())
            .collect(Collectors.toList()));
    parsedLines.addAll(
        parser
            .fetchForRange(new IPAddressString("1.0.3.16/28").getAddress())
            .collect(Collectors.toList()));

    List<MaxmindAnonymous> expected = new ArrayList<>();
    expected.add(
        MaxmindAnonymous.builder(new IPAddressString("1.0.2.0/25").getAddress())
            .isAnonymous(true)
            .isVpn(true)
            .build());
    expected.add(
        MaxmindAnonymous.builder(new IPAddressString("1.0.2.128/25").getAddress())
            .isAnonymous(true)
            .isVpn(true)
            .build());
    expected.add(
        MaxmindAnonymous.builder(new IPAddressString("1.0.3.16/28").getAddress())
            .isAnonymous(true)
            .isVpn(true)
            .build());

    Assert.assertEquals(expected, parsedLines);
  }
}
