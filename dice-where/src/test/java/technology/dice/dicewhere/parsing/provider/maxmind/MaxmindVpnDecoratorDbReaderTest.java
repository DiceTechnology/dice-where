/*
 * Copyright (C) 2018 - present by Dice Technology Ltd.
 *
 * Please see distribution for license.
 */

package technology.dice.dicewhere.parsing.provider.maxmind;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.common.collect.ImmutableList;
import inet.ipaddr.IPAddress;
import inet.ipaddr.IPAddressString;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import technology.dice.dicewhere.api.api.IP;
import technology.dice.dicewhere.api.exceptions.DecoratorDatabaseOutOfOrderException;
import technology.dice.dicewhere.decorator.VpnDecoratorInformation;
import technology.dice.dicewhere.provider.maxmind.decorator.MaxmindVpnDecoratorDbReader;

public class MaxmindVpnDecoratorDbReaderTest {

  @Test
  public void shouldThrowException_rangesOurOfOrders() throws IOException {
    assertThrows(
        DecoratorDatabaseOutOfOrderException.class,
        () -> {
          String ipv4Lines =
              "network,is_anonymous,is_anonymous_vpn,is_hosting_provider,is_public_proxy,is_tor_exit_node\n"
                  + "1.0.2.32/28,1,0,0,0,0\n"
                  + "1.0.2.55/32,1,1,0,0,0\n"
                  + "1.0.2.64/28,1,1,0,0,0\n"
                  + "1.0.4.0/27,1,1,0,0,0\n"
                  + "1.0.5.0/27,1,1,0,0,0\n"
                  + "1.0.7.32/28,1,1,0,0,0\n"
                  + "1.0.7.32/27,1,1,0,0,0\n"
                  + "1.0.7.48/28,1,1,0,0,0\n"
                  + "1.0.7.72/29,1,1,0,0,0\n"
                  + "1.0.7.96/29,1,1,0,0,0\n"
                  + "1.0.8.32/28,1,1,0,0,0\n"
                  + "1.0.8.32/27,1,1,0,0,0\n"
                  + "1.0.8.48/29,1,1,0,0,0\n"
                  + "1.0.8.72/29,1,1,0,0,0\n"
                  + "1.0.8.96/29,1,1,0,0,0";
          String ipv6Lines =
              "network,is_anonymous,is_anonymous_vpn,is_hosting_provider,is_public_proxy,is_tor_exit_node";
          InputStream streamV4 = new ByteArrayInputStream(ipv4Lines.getBytes());
          BufferedReader bufferedReaderV4 = new BufferedReader(new InputStreamReader(streamV4));
          InputStream streamV6 = new ByteArrayInputStream(ipv6Lines.getBytes());
          BufferedReader bufferedReaderV6 = new BufferedReader(new InputStreamReader(streamV6));
          MaxmindVpnDecoratorDbReader parser =
              new MaxmindVpnDecoratorDbReader(bufferedReaderV4, bufferedReaderV6);
          IPAddress inputAddress = new IPAddressString("1.0.7.0/24").getAddress();
          List<VpnDecoratorInformation> parsedLines =
              parser.fetchForRange(
                  new IP(inputAddress.getLower().getBytes()),
                  new IP(inputAddress.toMaxHost().getBytes()));
        },
        "Ranges out of line for 1.0.7.32 - 1.0.7.63");
  }

  @Test
  public void shouldParseIPv4() throws IOException {
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
    MaxmindVpnDecoratorDbReader parser =
        new MaxmindVpnDecoratorDbReader(bufferedReaderV4, bufferedReaderV6);
    IPAddress inputAddress = new IPAddressString("1.0.2.0/24").getAddress();
    List<VpnDecoratorInformation> parsedLines =
        parser.fetchForRange(
            new IP(inputAddress.getLower().getBytes()),
            new IP(inputAddress.toMaxHost().getBytes()));

    List<VpnDecoratorInformation> expected = new ArrayList<>();
    expected.add(
        new VpnDecoratorInformation(
            getLowerFromIP("1.0.2.16/28"), getMaxHostFromIP("1.0.2.16/28")));
    expected.add(
        new VpnDecoratorInformation(
            getLowerFromIP("1.0.2.55/32"), getMaxHostFromIP("1.0.2.55/32")));
    expected.add(
        new VpnDecoratorInformation(
            getLowerFromIP("1.0.2.64/28"), getMaxHostFromIP("1.0.2.64/28")));

    assertEquals(expected, parsedLines);
  }

  @Test
  public void shouldParseIPv4_whenAllRangesAreContainedInTheTarget() throws IOException {
    String ipv4Lines =
        "network,is_anonymous,is_anonymous_vpn,is_hosting_provider,is_public_proxy,is_tor_exit_node\n"
            + "1.0.2.32/28,1,0,0,0,0\n"
            + "1.0.2.55/32,1,1,0,0,0\n"
            + "1.0.2.64/28,1,1,0,0,0";

    String ipv6Lines =
        "network,is_anonymous,is_anonymous_vpn,is_hosting_provider,is_public_proxy,is_tor_exit_node";
    InputStream streamV4 = new ByteArrayInputStream(ipv4Lines.getBytes());
    BufferedReader bufferedReaderV4 = new BufferedReader(new InputStreamReader(streamV4));
    InputStream streamV6 = new ByteArrayInputStream(ipv6Lines.getBytes());
    BufferedReader bufferedReaderV6 = new BufferedReader(new InputStreamReader(streamV6));
    MaxmindVpnDecoratorDbReader parser =
        new MaxmindVpnDecoratorDbReader(bufferedReaderV4, bufferedReaderV6);
    IPAddress inputAddress = new IPAddressString("1.0.2.0/24").getAddress();
    List<VpnDecoratorInformation> parsedLines =
        parser.fetchForRange(
            new IP(inputAddress.getLower().getBytes()),
            new IP(inputAddress.toMaxHost().getBytes()));

    List<VpnDecoratorInformation> expected = new ArrayList<>();
    expected.add(
        new VpnDecoratorInformation(
            getLowerFromIP("1.0.2.55/32"), getMaxHostFromIP("1.0.2.55/32")));
    expected.add(
        new VpnDecoratorInformation(
            getLowerFromIP("1.0.2.64/28"), getMaxHostFromIP("1.0.2.64/28")));

    assertEquals(expected, parsedLines);
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
    MaxmindVpnDecoratorDbReader parser =
        new MaxmindVpnDecoratorDbReader(bufferedReaderV4, bufferedReaderV6);
    IPAddress inputAddress = new IPAddressString("1.0.2.0/24").getAddress();
    List<VpnDecoratorInformation> parsedLines =
        parser.fetchForRange(
            new IP(inputAddress.getLower().getBytes()),
            new IP(inputAddress.toMaxHost().getBytes()));

    assertTrue(parsedLines.isEmpty());
  }

  @Test
  public void shouldParseIPv6() throws IOException {
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
            + "2001:470:7:600::/55,1,1,0,0,0\n"
            + "2001:470:7:a00::/56,1,1,0,0,0\n"
            + "2001:470:7:b70::/62,1,1,0,0,0\n"
            + "2001:470:8:80::/59,1,1,0,0,0\n";
    InputStream streamV4 = new ByteArrayInputStream(ipv4Lines.getBytes());
    BufferedReader bufferedReaderV4 = new BufferedReader(new InputStreamReader(streamV4));
    InputStream streamV6 = new ByteArrayInputStream(ipv6Lines.getBytes());
    BufferedReader bufferedReaderV6 = new BufferedReader(new InputStreamReader(streamV6));
    MaxmindVpnDecoratorDbReader parser =
        new MaxmindVpnDecoratorDbReader(bufferedReaderV4, bufferedReaderV6);
    IPAddress inputAddress = new IPAddressString("2001:470:7:a00::/53").getAddress();
    List<VpnDecoratorInformation> parsedLines =
        parser.fetchForRange(
            new IP(inputAddress.getLower().getBytes()),
            new IP(inputAddress.toMaxHost().getBytes()));

    List<VpnDecoratorInformation> expected = new ArrayList<>();
    expected.add(
        new VpnDecoratorInformation(
            getLowerFromIP("2001:470:7:a00::/56"), getMaxHostFromIP("2001:470:7:a00::/56")));
    expected.add(
        new VpnDecoratorInformation(
            getLowerFromIP("2001:470:7:b70::/62"), getMaxHostFromIP("2001:470:7:b70::/62")));

    assertEquals(expected, parsedLines);
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
    MaxmindVpnDecoratorDbReader parser =
        new MaxmindVpnDecoratorDbReader(bufferedReaderV4, bufferedReaderV6);
    IPAddress inputAddress = new IPAddressString("1.0.2.0/25").getAddress();
    List<VpnDecoratorInformation> parsedLines1 =
        parser.fetchForRange(
            new IP(inputAddress.getLower().getBytes()),
            new IP(inputAddress.toMaxHost().getBytes()));
    inputAddress = new IPAddressString("1.0.2.128/25").getAddress();
    List<VpnDecoratorInformation> parsedLines2 =
        parser.fetchForRange(
            new IP(inputAddress.getLower().getBytes()),
            new IP(inputAddress.toMaxHost().getBytes()));
    inputAddress = new IPAddressString("1.0.3.16/28").getAddress();
    List<VpnDecoratorInformation> parsedLines3 =
        parser.fetchForRange(
            new IP(inputAddress.getLower().getBytes()),
            new IP(inputAddress.toMaxHost().getBytes()));

    List<VpnDecoratorInformation> parsedLines =
        ImmutableList.<VpnDecoratorInformation>builder()
            .addAll(parsedLines1)
            .addAll(parsedLines2)
            .addAll(parsedLines3)
            .build();

    List<VpnDecoratorInformation> expected = new ArrayList<>();
    expected.add(
        new VpnDecoratorInformation(getLowerFromIP("1.0.2.0/25"), getMaxHostFromIP("1.0.2.0/25")));
    expected.add(
        new VpnDecoratorInformation(
            getLowerFromIP("1.0.2.128/25"), getMaxHostFromIP("1.0.2.128/25")));
    expected.add(
        new VpnDecoratorInformation(
            getLowerFromIP("1.0.3.16/28"), getMaxHostFromIP("1.0.3.16/28")));

    assertEquals(expected, parsedLines);
  }

  IP getLowerFromIP(String input) {
    return new IP(new IPAddressString(input).getAddress().getLower().getBytes());
  }

  IP getMaxHostFromIP(String input) {
    return new IP(new IPAddressString(input).getAddress().toMaxHost().getBytes());
  }
}
