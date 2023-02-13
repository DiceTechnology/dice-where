/*
 * Copyright (C) 2018 - present by Dice Technology Ltd.
 *
 * Please see distribution for license.
 */

package technology.dice.dicewhere.decorator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import inet.ipaddr.IPAddress;
import inet.ipaddr.IPAddressString;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import technology.dice.dicewhere.api.api.IP;
import technology.dice.dicewhere.api.api.IpInformation;

public class VpnDecoratorTest {

  @Test()
  public void shouldThrowNpe() {
    assertThrows(
        NullPointerException.class,
        () -> {
          VpnDecorator decorator =
              DecoratorTestUtils.getMaxmindVpnDecorator(DecorationStrategy.ANY);
          decorator.decorate(null);
        });
  }

  @Test
  public void shouldDecorateMultipleOverlappingRanges_case2_allMergeStrategy() throws IOException {
    VpnDecorator decorator = DecoratorTestUtils.getMaxmindVpnDecorator(DecorationStrategy.ALL);
    IPAddress inputAddress = new IPAddressString("1.0.8.0/24").getAddress();
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
            .withStartOfRange(getLowerFromIP("1.0.8.0/32"))
            .withEndOfRange(getMaxHostFromIP("1.0.8.31/32"))
            .isVpn(false)
            .build());
    expected.add(
        IpInformation.builder(target)
            .withStartOfRange(getLowerFromIP("1.0.8.32/32"))
            .withEndOfRange(getMaxHostFromIP("1.0.8.39/32"))
            .isVpn(true)
            .build());
    expected.add(
        IpInformation.builder(target)
            .withStartOfRange(getLowerFromIP("1.0.8.40/32"))
            .withEndOfRange(getMaxHostFromIP("1.0.8.0/24"))
            .isVpn(false)
            .build());

    assertEquals(expected, actual);
  }

  @Test
  public void shouldDecorateMultipleOverlappingRanges_case2_majorityMergeStrategy()
      throws IOException {
    VpnDecorator decorator = DecoratorTestUtils.getMaxmindVpnDecorator(DecorationStrategy.MAJORITY);
    IPAddress inputAddress = new IPAddressString("1.0.7.0/24").getAddress();
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
            .withStartOfRange(getLowerFromIP("1.0.7.0/32"))
            .withEndOfRange(getMaxHostFromIP("1.0.7.15/32"))
            .isVpn(false)
            .build());
    expected.add(
        IpInformation.builder(target)
            .withStartOfRange(getLowerFromIP("1.0.7.16/32"))
            .withEndOfRange(getMaxHostFromIP("1.0.7.23/32"))
            .isVpn(true)
            .build());
    expected.add(
        IpInformation.builder(target)
            .withStartOfRange(getLowerFromIP("1.0.7.24/32"))
            .withEndOfRange(getMaxHostFromIP("1.0.7.31/32"))
            .isVpn(false)
            .build());

    expected.add(
        IpInformation.builder(target)
            .withStartOfRange(getLowerFromIP("1.0.7.32/32"))
            .withEndOfRange(getMaxHostFromIP("1.0.7.63/32"))
            .isVpn(true)
            .build());
    expected.add(
        IpInformation.builder(target)
            .withStartOfRange(getLowerFromIP("1.0.7.64/32"))
            .withEndOfRange(getMaxHostFromIP("1.0.7.71/32"))
            .isVpn(false)
            .build());
    expected.add(
        IpInformation.builder(target)
            .withStartOfRange(getLowerFromIP("1.0.7.72/32"))
            .withEndOfRange(getMaxHostFromIP("1.0.7.79/32"))
            .isVpn(true)
            .build());
    expected.add(
        IpInformation.builder(target)
            .withStartOfRange(getLowerFromIP("1.0.7.80/32"))
            .withEndOfRange(getMaxHostFromIP("1.0.7.95/32"))
            .isVpn(false)
            .build());
    expected.add(
        IpInformation.builder(target)
            .withStartOfRange(getLowerFromIP("1.0.7.96/32"))
            .withEndOfRange(getMaxHostFromIP("1.0.7.103/32"))
            .isVpn(true)
            .build());
    expected.add(
        IpInformation.builder(target)
            .withStartOfRange(getLowerFromIP("1.0.7.104/32"))
            .withEndOfRange(getMaxHostFromIP("1.0.7.0/24"))
            .isVpn(false)
            .build());

    assertEquals(expected, actual);
  }

  @Test
  public void shouldDecorateMultipleOverlappingRanges_case2_anyMergeStrategy() throws IOException {
    VpnDecorator decorator = DecoratorTestUtils.getMaxmindVpnDecorator(DecorationStrategy.ANY);
    IPAddress inputAddress = new IPAddressString("1.0.7.0/24").getAddress();
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
            .withStartOfRange(getLowerFromIP("1.0.7.0/24"))
            .withEndOfRange(getMaxHostFromIP("1.0.7.0/25"))
            .isVpn(true)
            .build());
    expected.add(
        IpInformation.builder(target)
            .withStartOfRange(getLowerFromIP("1.0.7.128/25"))
            .withEndOfRange(getMaxHostFromIP("1.0.7.0/24"))
            .isVpn(false)
            .build());

    assertEquals(expected, actual);
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

    assertEquals(expected, actual);
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

    assertEquals(expected, actual);
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

    assertEquals(expected, actual);
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
    assertEquals(target, actual);
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
    assertEquals(expected, actual);
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
    assertEquals(target, actual);
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
    assertEquals(expected, actual);
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
    assertEquals(expected, actual);
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

    assertEquals(expected, actual);
  }

  IP getLowerFromIP(String input) {
    return new IP(new IPAddressString(input).getAddress().getLower().getBytes());
  }

  IP getMaxHostFromIP(String input) {
    return new IP(new IPAddressString(input).getAddress().toMaxHost().getBytes());
  }
}
