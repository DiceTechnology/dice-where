/*
 * Copyright (C) 2018 - present by Dice Technology Ltd.
 *
 * Please see distribution for license.
 */

package technology.dice.dicewhere.decorator;

import technology.dice.dicewhere.provider.maxmind.decorator.MaxmindVpnDecoratorDbReader;

import java.io.*;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DecoratorTestUtils {

  public static final String IPv4_LINES =
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
          + "1.0.5.32/28,1,1,0,0,0\n"
          + "1.0.7.16/29,1,1,0,0,0\n"
          + "1.0.8.16/29,1,1,0,0,0\n"
          + "1.0.8.32/29,1,1,0,0,0\n"
          + "1.0.32.1/32,1,1,0,0,0";

  public static final String IPv6_LINES =
      "network,is_anonymous,is_anonymous_vpn,is_hosting_provider,is_public_proxy,is_tor_exit_node\n"
          + "2001:470:7:600::/55,1,1,0,0,0\n"
          + "2001:470:7:a00::/56,1,1,0,0,0\n"
          + "2001:470:7:b70::/62,1,1,0,0,0\n"
          + "2001:470:8:80::/59,1,1,0,0,0";
  public static final String IPv4_LINES_2 =
      "network,is_anonymous,is_anonymous_vpn,is_hosting_provider,is_public_proxy,is_tor_exit_node\n"
          + "1.0.2.32/28,1,0,0,0,0\n"
          + "1.0.2.55/32,1,1,0,0,0\n"
          + "1.0.2.64/28,1,1,0,0,0\n"
          + "1.0.4.0/27,1,1,0,0,0\n"
          + "1.0.5.0/27,1,1,0,0,0\n"
          + "1.0.7.32/28,1,1,0,0,0\n"
//          + "1.0.7.32/27,1,1,0,0,0\n"
          + "1.0.7.48/28,1,1,0,0,0\n"
          + "1.0.7.72/29,1,1,0,0,0\n"
          + "1.0.7.96/29,1,1,0,0,0\n"
		  + "1.0.8.32/28,1,1,0,0,0\n"
//		  + "1.0.8.32/27,1,1,0,0,0\n"
		  + "1.0.8.48/29,1,1,0,0,0\n"
		  + "1.0.8.72/29,1,1,0,0,0\n"
		  + "1.0.8.96/29,1,1,0,0,0";

  public static final String IPv6_LINES_2 =
      "network,is_anonymous,is_anonymous_vpn,is_hosting_provider,is_public_proxy,is_tor_exit_node";

  public static final String IPv4_LINES_3 =
      "network,is_anonymous,is_anonymous_vpn,is_hosting_provider,is_public_proxy,is_tor_exit_node\n"
          + "1.0.2.16/28,1,1,0,0,0\n"
          + "1.0.2.64/28,1,1,0,0,0\n"
          + "1.0.3.64/28,1,1,0,0,0\n"
          + "1.0.4.0/26,1,1,0,0,0\n"
          + "1.0.5.0/26,1,1,0,0,0\n"
          + "1.0.7.0/25,1,1,0,0,0\n"
          + "1.0.8.0/25,1,1,0,0,0";

  public static final String IPv6_LINES_3 =
      "network,is_anonymous,is_anonymous_vpn,is_hosting_provider,is_public_proxy,is_tor_exit_node";

  public static VpnDecorator getMaxmindVpnDecorator(
      DecorationStrategy strategy, List<String> ipv4Lines, List<String> ipv6Lines)
      throws IOException {
    Stream.Builder<MaxmindVpnDecoratorDbReader> builder = Stream.builder();
    for (int i = 0; i < ipv4Lines.size(); i++) {
      builder.add(getMaxmindVpnDecorator(ipv4Lines.get(i), ipv6Lines.get(i)));
    }
    return new VpnDecorator(builder.build().collect(Collectors.toList()), strategy);
  }

  public static VpnDecorator getMaxmindVpnDecorator(DecorationStrategy strategy)
      throws IOException {
    return new VpnDecorator(
        Stream.<MaxmindVpnDecoratorDbReader>builder()
            .add(getMaxmindVpnDecorator(IPv4_LINES, IPv6_LINES))
            .add(getMaxmindVpnDecorator(IPv4_LINES_2, IPv6_LINES_2))
            .add(getMaxmindVpnDecorator(IPv4_LINES_3, IPv6_LINES_3))
            .build()
            .collect(Collectors.toList()),
        strategy);
  }

  public static MaxmindVpnDecoratorDbReader getMaxmindVpnDecorator(
      String ipv4Lines, String ipv6Lines) throws IOException {
    InputStream streamV4 = new ByteArrayInputStream(ipv4Lines.getBytes());
    BufferedReader bufferedReaderV4 = new BufferedReader(new InputStreamReader(streamV4));
    InputStream streamV6 = new ByteArrayInputStream(ipv6Lines.getBytes());
    BufferedReader bufferedReaderV6 = new BufferedReader(new InputStreamReader(streamV6));
    return new MaxmindVpnDecoratorDbReader(bufferedReaderV4, bufferedReaderV6);
  }
}
