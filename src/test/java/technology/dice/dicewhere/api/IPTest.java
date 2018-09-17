/*
 * Copyright (C) 2018 - present by Dice Technology Ltd.
 *
 * Please see distribution for license.
 */

package technology.dice.dicewhere.api;

import com.google.common.net.InetAddresses;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Iterator;

import inet.ipaddr.IPAddress;
import inet.ipaddr.IPAddressString;
import inet.ipaddr.ipv6.IPv6Address;
import org.junit.Assert;
import org.junit.Test;
import technology.dice.dicewhere.api.api.IP;

//TODO: add ipv6 tests
public class IPTest {
  @Test(expected = NullPointerException.class)
  public void bytesCtorNull() {
    new IP((byte[]) null);
  }

  @Test(expected = NullPointerException.class)
  public void inetAddressCtorNull() {
    new IP((InetAddress) null);
  }

  @Test
  public void validFromBytes() {
    new IP(new byte[] {0, 0, 0, 0, 0, 0, 0});
  }

  @Test
  public void validFromInetAddress() {
    new IP(InetAddresses.forString("1.1.1.1"));
  }

  @Test
  public void isLower() {
    IP anIP = new IP(InetAddresses.forString("1.1.1.1"));
    IP lowerIp = new IP(InetAddresses.forString("1.1.1.0"));
    Assert.assertTrue(lowerIp.isLowerThan(anIP));
    Assert.assertFalse(anIP.isLowerThan(lowerIp));
    Assert.assertFalse(anIP.equals(lowerIp));
  }

  @Test
  public void isHigher() {
    IP anIP = new IP(InetAddresses.forString("1.1.1.1"));
    IP higherIp = new IP(InetAddresses.forString("1.1.1.2"));
    Assert.assertTrue(higherIp.isGreaterThan(anIP));
    Assert.assertFalse(anIP.isGreaterThan(higherIp));
    Assert.assertFalse(anIP.equals(higherIp));
  }

  @Test
  public void ipv6() {
    IPAddressString rangeStringStr = new IPAddressString("2001:470:7:a00::/53");
    IPv6Address rangeString = rangeStringStr.getAddress().toPrefixBlock().toIPv6();
    System.out.println(rangeString.getLower().toString()); // produces 2001:470:7:800:0:0:0:0/53
    System.out.println(rangeString.getUpper().toString()); // produces 2001:470:7:fff:ffff:ffff:ffff:ffff/53
  }


}
