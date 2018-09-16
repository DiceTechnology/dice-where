/*
 * Copyright (C) 2018 - present by Dice Technology Ltd.
 *
 * Please see distribution for license.
 */

package technology.dice.dicewhere.api;

import com.google.common.net.InetAddresses;
import java.net.InetAddress;
import java.util.Iterator;

import inet.ipaddr.IPAddress;
import inet.ipaddr.IPAddressString;
import org.junit.Assert;
import org.junit.Test;
import technology.dice.dicewhere.api.api.IP;

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
  public void tests() {
    //1.0.2.0/23
    //  1.0.2.16/28
    //  1.0.2.64/28
//    IPAddress address = new IPAddressString("1.0.2.0/23").getAddress();
    IPAddress address = new IPAddressString("2000:db8::/32").getAddress();
    IPAddress subRange1 = new IPAddressString("1.0.2.16/28").getAddress();
    IP subRange1L = new IP(subRange1.getLower().getBytes());
    IP subRange1U = new IP(subRange1.getUpper().getBytes());
    IPAddress subRange2 = new IPAddressString("1.0.2.64/28").getAddress();
    IP subRange2L = new IP(subRange2.getLower().getBytes());
    IP subRange2U = new IP(subRange2.getUpper().getBytes());

    IPAddress subRange3 = new IPAddressString("1.0.3.16/28").getAddress();
    IP subRange3L = new IP(subRange1.getLower().getBytes());
    IP subRange3U = new IP(subRange1.getUpper().getBytes());
    IPAddress subRange4 = new IPAddressString("1.0.3.64/28").getAddress();
    IP subRange4L = new IP(subRange2.getLower().getBytes());
    IP subRange4U = new IP(subRange2.getUpper().getBytes());
    Iterator<? extends IPAddress> i = address.getIterable().iterator();
    while(i.hasNext()) {
      IPAddress a = i.next();
      IP al = new IP(a.getLower().getBytes());
      IP au = new IP(a.getUpper().getBytes());
      boolean nestedRange1 = subRange1L.isLowerThanOrEqual(al) && subRange1U.isGreaterThanOrEqual(au);
      boolean nestedRange2 = subRange2L.isLowerThanOrEqual(al) && subRange2U.isGreaterThanOrEqual(au);
      System.out.println(String.format("%s - Nested range 1: %s, Nested range 2: %s", a.toAddressString(), nestedRange1, nestedRange2) );
    }
  }

}
