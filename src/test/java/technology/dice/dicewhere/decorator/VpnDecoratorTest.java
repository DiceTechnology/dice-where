/*
 * Copyright (C) 2018 - present by Dice Technology Ltd.
 *
 * Please see distribution for license.
 */

package technology.dice.dicewhere.decorator;

import inet.ipaddr.IPAddress;
import inet.ipaddr.IPAddressString;
import org.junit.Assert;
import org.junit.Test;
import technology.dice.dicewhere.api.api.IP;
import technology.dice.dicewhere.api.api.IpInformation;
import technology.dice.dicewhere.provider.maxmind.decorator.MaxmindVpnDecoratorDbReader;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class VpnDecoratorTest {

  private String IPv4_LINES =
      "network,is_anonymous,is_anonymous_vpn,is_hosting_provider,is_public_proxy,is_tor_exit_node\n"
          + "1.0.1.0/28,1,1,0,0,0\n"
          + "1.0.1.16/28,1,1,0,0,0\n"
          + "1.0.2.16/28,1,1,0,0,0\n"
          + "1.0.2.64/28,1,1,0,0,0\n"
          + "1.0.3.16/28,1,1,0,0,0\n"
          + "1.0.3.32/28,1,0,0,0,0\n"
          + "1.0.3.64/28,1,1,0,0,0\n"
          + "1.0.4.0/28,1,1,0,0,0\n"
          + "1.0.4.16/28,1,1,0,0,0\n"
          + "1.0.5.0/28,1,1,0,0,0\n"
          + "1.0.5.32/28,1,1,0,0,0";

  private String IPv6_LINES =
      "network,is_anonymous,is_anonymous_vpn,is_hosting_provider,is_public_proxy,is_tor_exit_node\n"
          + "2001:470:7:600::/55,1,1,0,0,0\n"
          + "2001:470:7:a00::/56,1,1,0,0,0\n"
          + "2001:470:7:b70::/62,1,1,0,0,0\n"
          + "2001:470:8:80::/59,1,1,0,0,0";
  private String IPv4_LINES_2 =
      "network,is_anonymous,is_anonymous_vpn,is_hosting_provider,is_public_proxy,is_tor_exit_node\n"
          + "1.0.2.32/28,1,0,0,0,0\n"
          + "1.0.2.55/32,1,1,0,0,0\n"
          + "1.0.2.64/28,1,1,0,0,0\n"
          + "1.0.4.0/27,1,1,0,0,0\n"
          + "1.0.5.0/27,1,1,0,0,0";
  private String IPv6_LINES_2 =
      "network,is_anonymous,is_anonymous_vpn,is_hosting_provider,is_public_proxy,is_tor_exit_node";

  private String IPv4_LINES_3 =
      "network,is_anonymous,is_anonymous_vpn,is_hosting_provider,is_public_proxy,is_tor_exit_node\n"
          + "1.0.2.16/28,1,1,0,0,0\n"
          + "1.0.2.64/28,1,1,0,0,0\n"
          + "1.0.3.64/28,1,1,0,0,0\n"
          + "1.0.4.0/26,1,1,0,0,0\n"
          + "1.0.5.0/26,1,1,0,0,0";

  private String IPv6_LINES_3 =
      "network,is_anonymous,is_anonymous_vpn,is_hosting_provider,is_public_proxy,is_tor_exit_node";

  @Test(expected = NullPointerException.class)
  public void shouldThrowNpe() throws IOException {
    VpnDecorator decorator = getDecorator(DecorationStrategy.ANY);
    decorator.decorate(null);
  }

  @Test
  public void shouldDecorateMultipleOverlappingRanges_anyMergeStrategy() throws IOException {
    VpnDecorator decorator = getDecorator(DecorationStrategy.ANY);
    IPAddress inputAddress = new IPAddressString("1.0.4.0/24").getAddress();
    IpInformation target =
        IpInformation.builder()
            .withStartOfRange(new IP(inputAddress.getLower().getBytes()))
            .withEndOfRange(new IP(inputAddress.toMaxHost().getBytes()))
            .withCountryCodeAlpha2("BG")
            .withGeonameId("111")
            .build();
    List<IpInformation> actual = decorator.decorate(target).collect(Collectors.toList());
    List<IpInformation> expected = new ArrayList<>();
    expected.add(
        IpInformation.builder(target)
            .withStartOfRange(getLowerFromIP("1.0.4.0/27"))
            .withEndOfRange(getMaxHostFromIP("1.0.4.63/32"))
            .isVpn(true)
            .build());
    expected.add(
        IpInformation.builder(target)
            .withStartOfRange(getLowerFromIP("1.0.4.64/32"))
            .withEndOfRange(getMaxHostFromIP("1.0.4.0/24"))
            .isVpn(false)
            .build());

    Assert.assertEquals(expected, actual);
  }

  @Test
  public void shouldDecorateMultipleOverlappingRanges_majorityMergeStrategy() throws IOException {
    VpnDecorator decorator = getDecorator(DecorationStrategy.MAJORITY);
    IPAddress inputAddress = new IPAddressString("1.0.5.0/24").getAddress();
    IpInformation target =
        IpInformation.builder()
            .withStartOfRange(new IP(inputAddress.getLower().getBytes()))
            .withEndOfRange(new IP(inputAddress.toMaxHost().getBytes()))
            .withCountryCodeAlpha2("BG")
            .withGeonameId("111")
            .build();
    List<IpInformation> actual = decorator.decorate(target).collect(Collectors.toList());
    List<IpInformation> expected = new ArrayList<>();
    expected.add(
        IpInformation.builder(target)
            .withStartOfRange(getLowerFromIP("1.0.5.0/27"))
            .withEndOfRange(getMaxHostFromIP("1.0.5.47/32"))
            .isVpn(true)
            .build());
    expected.add(
        IpInformation.builder(target)
            .withStartOfRange(getLowerFromIP("1.0.5.48/32"))
            .withEndOfRange(getMaxHostFromIP("1.0.5.0/24"))
            .isVpn(false)
            .build());

    Assert.assertEquals(expected, actual);
  }

  @Test
  public void shouldReturnOriginalInfoCompletelyDecorated_wholeRegionIsVpn() throws IOException {
    VpnDecorator decorator = getDecorator(DecorationStrategy.ANY);
    IPAddress inputAddress = new IPAddressString("1.0.1.0/27").getAddress();
    IpInformation target =
        IpInformation.builder()
            .withStartOfRange(new IP(inputAddress.getLower().getBytes()))
            .withEndOfRange(new IP(inputAddress.toMaxHost().getBytes()))
            .withCountryCodeAlpha2("BG")
            .withGeonameId("111")
            .build();
    List<IpInformation> actual = decorator.decorate(target).collect(Collectors.toList());
    List<IpInformation> expected = new ArrayList<>();
    expected.add(
        IpInformation.builder(target)
            .withStartOfRange(getLowerFromIP("1.0.1.0/27"))
            .withEndOfRange(getMaxHostFromIP("1.0.1.0/27"))
            .isVpn(true)
            .build());

    Assert.assertEquals(expected, actual);
  }

  @Test
  public void shouldReturnOriginalInfo_noVpnRangesMatch() throws IOException {
    VpnDecorator decorator = getDecorator(DecorationStrategy.ANY);
    IPAddress inputAddress = new IPAddressString("1.0.6.0/24").getAddress();
    IpInformation target =
        IpInformation.builder()
            .withStartOfRange(new IP(inputAddress.getLower().getBytes()))
            .withEndOfRange(new IP(inputAddress.toMaxHost().getBytes()))
            .withCountryCodeAlpha2("BG")
            .withGeonameId("111")
            .build();
    IpInformation actual = decorator.decorate(target).collect(Collectors.toList()).get(0);
    Assert.assertEquals(target, actual);
  }

  @Test
  public void shouldReturnOriginalInfoDecorated_matchesExactlyARange() throws IOException {
    VpnDecorator decorator = getDecorator(DecorationStrategy.ANY);
    IPAddress inputAddress = new IPAddressString("1.0.2.16/28").getAddress();
    IpInformation target =
        IpInformation.builder()
            .withStartOfRange(new IP(inputAddress.getLower().getBytes()))
            .withEndOfRange(new IP(inputAddress.toMaxHost().getBytes()))
            .withCountryCodeAlpha2("BG")
            .withGeonameId("111")
            .build();
    IpInformation actual = decorator.decorate(target).collect(Collectors.toList()).get(0);
    IpInformation expected = IpInformation.builder(target).isVpn(true).build();
    Assert.assertEquals(expected, actual);
  }

  @Test
  public void shouldReturnOriginalInfoNotDecorated_doesntMatchDecorationStrategy()
      throws IOException {
    VpnDecorator decorator = getDecorator(DecorationStrategy.ALL);
    IPAddress inputAddress = new IPAddressString("1.0.2.16/28").getAddress();
    IpInformation target =
        IpInformation.builder()
            .withStartOfRange(new IP(inputAddress.getLower().getBytes()))
            .withEndOfRange(new IP(inputAddress.toMaxHost().getBytes()))
            .withCountryCodeAlpha2("BG")
            .withGeonameId("111")
            .build();
    IpInformation actual = decorator.decorate(target).collect(Collectors.toList()).get(0);
    Assert.assertEquals(target, actual);
  }

  @Test
  public void shouldReturnRangesThatExistInAllDecorators() throws IOException {
    VpnDecorator decorator = getDecorator(DecorationStrategy.ALL);
    IPAddress inputAddress = new IPAddressString("1.0.2.0/24").getAddress();
    IpInformation target =
        IpInformation.builder()
            .withStartOfRange(new IP(inputAddress.getLower().getBytes()))
            .withEndOfRange(new IP(inputAddress.toMaxHost().getBytes()))
            .withCountryCodeAlpha2("BG")
            .withGeonameId("111")
            .build();
    List<IpInformation> actual = decorator.decorate(target).collect(Collectors.toList());
    List<IpInformation> expected = new ArrayList<>();
    expected.add(
        IpInformation.builder(target)
            .withStartOfRange(getLowerFromIP("1.0.2.0/24"))
            .withEndOfRange(getMaxHostFromIP("1.0.2.63/32"))
            .isVpn(false)
            .build());
    expected.add(
        IpInformation.builder(target)
            .withStartOfRange(getLowerFromIP("1.0.2.64/32"))
            .withEndOfRange(getMaxHostFromIP("1.0.2.79/32"))
            .isVpn(true)
            .build());
    expected.add(
        IpInformation.builder(target)
            .withStartOfRange(getLowerFromIP("1.0.2.80/32"))
            .withEndOfRange(getMaxHostFromIP("1.0.2.0/24"))
            .isVpn(false)
            .build());
    Assert.assertEquals(expected, actual);
  }

  @Test
  public void shouldReturnRangesThatExistInMajoritylDecorators() throws IOException {
    VpnDecorator decorator = getDecorator(DecorationStrategy.MAJORITY);
    IPAddress inputAddress = new IPAddressString("1.0.2.0/24").getAddress();
    IpInformation target =
        IpInformation.builder()
            .withStartOfRange(new IP(inputAddress.getLower().getBytes()))
            .withEndOfRange(new IP(inputAddress.toMaxHost().getBytes()))
            .withCountryCodeAlpha2("BG")
            .withGeonameId("111")
            .build();
    List<IpInformation> actual = decorator.decorate(target).collect(Collectors.toList());
    List<IpInformation> expected = new ArrayList<>();
    expected.add(
        IpInformation.builder(target)
            .withStartOfRange(getLowerFromIP("1.0.2.0/24"))
            .withEndOfRange(getMaxHostFromIP("1.0.2.15/32"))
            .isVpn(false)
            .build());
    expected.add(
        IpInformation.builder(target)
            .withStartOfRange(getLowerFromIP("1.0.2.16/32"))
            .withEndOfRange(getMaxHostFromIP("1.0.2.31/32"))
            .isVpn(true)
            .build());
    expected.add(
        IpInformation.builder(target)
            .withStartOfRange(getLowerFromIP("1.0.2.32/32"))
            .withEndOfRange(getMaxHostFromIP("1.0.2.63/32"))
            .isVpn(false)
            .build());
    expected.add(
        IpInformation.builder(target)
            .withStartOfRange(getLowerFromIP("1.0.2.64/32"))
            .withEndOfRange(getMaxHostFromIP("1.0.2.79/32"))
            .isVpn(true)
            .build());
    expected.add(
        IpInformation.builder(target)
            .withStartOfRange(getLowerFromIP("1.0.2.80/32"))
            .withEndOfRange(getMaxHostFromIP("1.0.2.0/24"))
            .isVpn(false)
            .build());
    Assert.assertEquals(expected, actual);
  }

  @Test
  public void shouldReturnRangesFromBothDecorators() throws IOException {
    VpnDecorator decorator = getDecorator(DecorationStrategy.ANY);
    IPAddress inputAddress = new IPAddressString("1.0.2.0/24").getAddress();
    IpInformation target =
        IpInformation.builder()
            .withStartOfRange(new IP(inputAddress.getLower().getBytes()))
            .withEndOfRange(new IP(inputAddress.toMaxHost().getBytes()))
            .withCountryCodeAlpha2("BG")
            .withGeonameId("111")
            .build();
    List<IpInformation> actual = decorator.decorate(target).collect(Collectors.toList());
    List<IpInformation> expected = new ArrayList<>();
    expected.add(
        IpInformation.builder(target)
            .withStartOfRange(getLowerFromIP("1.0.2.0/24"))
            .withEndOfRange(getMaxHostFromIP("1.0.2.15/32"))
            .isVpn(false)
            .build());
    expected.add(
        IpInformation.builder(target)
            .withStartOfRange(getLowerFromIP("1.0.2.16/32"))
            .withEndOfRange(getMaxHostFromIP("1.0.2.31/32"))
            .isVpn(true)
            .build());
    expected.add(
        IpInformation.builder(target)
            .withStartOfRange(getLowerFromIP("1.0.2.32/32"))
            .withEndOfRange(getMaxHostFromIP("1.0.2.54/32"))
            .isVpn(false)
            .build());
    expected.add(
        IpInformation.builder(target)
            .withStartOfRange(getLowerFromIP("1.0.2.55/32"))
            .withEndOfRange(getMaxHostFromIP("1.0.2.55/32"))
            .isVpn(true)
            .build());
    expected.add(
        IpInformation.builder(target)
            .withStartOfRange(getLowerFromIP("1.0.2.56/32"))
            .withEndOfRange(getMaxHostFromIP("1.0.2.63/32"))
            .isVpn(false)
            .build());
    expected.add(
        IpInformation.builder(target)
            .withStartOfRange(getLowerFromIP("1.0.2.64/32"))
            .withEndOfRange(getMaxHostFromIP("1.0.2.79/32"))
            .isVpn(true)
            .build());
    expected.add(
        IpInformation.builder(target)
            .withStartOfRange(getLowerFromIP("1.0.2.80/32"))
            .withEndOfRange(getMaxHostFromIP("1.0.2.0/24"))
            .isVpn(false)
            .build());

    Assert.assertEquals(expected, actual);
  }

  private VpnDecorator getDecorator(DecorationStrategy strategy) throws IOException {
    return new VpnDecorator(
        Stream.<MaxmindVpnDecoratorDbReader>builder()
            .add(getDecorator(IPv4_LINES, IPv6_LINES))
            .add(getDecorator(IPv4_LINES_2, IPv6_LINES_2))
            .add(getDecorator(IPv4_LINES_3, IPv6_LINES_3))
            .build()
            .collect(Collectors.toList()),
        strategy);
  }

  private MaxmindVpnDecoratorDbReader getDecorator(String ipv4Lines, String ipv6Lines)
      throws IOException {
    InputStream streamV4 = new ByteArrayInputStream(ipv4Lines.getBytes());
    BufferedReader bufferedReaderV4 = new BufferedReader(new InputStreamReader(streamV4));
    InputStream streamV6 = new ByteArrayInputStream(ipv6Lines.getBytes());
    BufferedReader bufferedReaderV6 = new BufferedReader(new InputStreamReader(streamV6));
    return new MaxmindVpnDecoratorDbReader(bufferedReaderV4, bufferedReaderV6);
  }

  IP getLowerFromIP(String input) {
    return new IP(new IPAddressString(input).getAddress().getLower().getBytes());
  }

  IP getMaxHostFromIP(String input) {
    return new IP(new IPAddressString(input).getAddress().toMaxHost().getBytes());
  }
}
