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

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class VpnDecoratorTest {


  @Test(expected = NullPointerException.class)
  public void shouldThrowNpe() throws IOException {
    VpnDecorator decorator = DecoratorTestUtils.getMaxmindVpnDecorator(DecorationStrategy.ANY);
    decorator.decorate(null);
  }

  @Test
  public void shouldDecorateMultipleOverlappingRanges_anyMergeStrategy() throws IOException {
    VpnDecorator decorator = DecoratorTestUtils.getMaxmindVpnDecorator(DecorationStrategy.ANY);
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
    VpnDecorator decorator = DecoratorTestUtils.getMaxmindVpnDecorator(DecorationStrategy.MAJORITY);
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
    VpnDecorator decorator = DecoratorTestUtils.getMaxmindVpnDecorator(DecorationStrategy.ANY);
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
    VpnDecorator decorator = DecoratorTestUtils.getMaxmindVpnDecorator(DecorationStrategy.ANY);
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
    VpnDecorator decorator = DecoratorTestUtils.getMaxmindVpnDecorator(DecorationStrategy.ANY);
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
    VpnDecorator decorator = DecoratorTestUtils.getMaxmindVpnDecorator(DecorationStrategy.ALL);
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
    VpnDecorator decorator = DecoratorTestUtils.getMaxmindVpnDecorator(DecorationStrategy.ALL);
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
    VpnDecorator decorator = DecoratorTestUtils.getMaxmindVpnDecorator(DecorationStrategy.MAJORITY);
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
    VpnDecorator decorator = DecoratorTestUtils.getMaxmindVpnDecorator(DecorationStrategy.ANY);
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



  IP getLowerFromIP(String input) {
    return new IP(new IPAddressString(input).getAddress().getLower().getBytes());
  }

  IP getMaxHostFromIP(String input) {
    return new IP(new IPAddressString(input).getAddress().toMaxHost().getBytes());
  }
}
