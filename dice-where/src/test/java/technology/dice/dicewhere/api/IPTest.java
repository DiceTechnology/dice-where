/*
 * Copyright (C) 2018 - present by Dice Technology Ltd.
 *
 * Please see distribution for license.
 */

package technology.dice.dicewhere.api;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.common.net.InetAddresses;
import java.net.InetAddress;
import org.junit.jupiter.api.Test;
import technology.dice.dicewhere.api.api.IP;

// TODO: add ipv6 tests
public class IPTest {
  @Test
  public void bytesCtorNull() {
    assertThrows(NullPointerException.class, () -> new IP((byte[]) null));
  }

  @Test
  public void inetAddressCtorNull() {
    assertThrows(NullPointerException.class, () -> new IP((InetAddress) null));
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
    assertTrue(lowerIp.isLowerThan(anIP));
    assertFalse(anIP.isLowerThan(lowerIp));
    assertFalse(anIP.equals(lowerIp));
  }

  @Test
  public void isHigher() {
    IP anIP = new IP(InetAddresses.forString("1.1.1.1"));
    IP higherIp = new IP(InetAddresses.forString("1.1.1.2"));
    assertTrue(higherIp.isGreaterThan(anIP));
    assertFalse(anIP.isGreaterThan(higherIp));
    assertFalse(anIP.equals(higherIp));
  }
  //
  //  @Test
  //  public void ipv6Bounds() {
  //    IPAddressString rangeStringStr = new IPAddressString("2001:470:7:a00::/53");
  //    IPv6Address rangeString = rangeStringStr.getAddress().toPrefixBlock().toIPv6();
  //    System.out.println(rangeString.getLower().toString()); // produces 2001:470:7:800:0:0:0:0/53
  //    System.out.println(rangeString.getUpper().toString()); // produces
  // 2001:470:7:fff:ffff:ffff:ffff:ffff/53
  //  }

}
