package technology.dice.dicewhere.api;

import com.google.common.net.InetAddresses;
import org.junit.Assert;
import org.junit.Test;
import technology.dice.dicewhere.api.api.IP;

import java.net.InetAddress;

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
		new IP(new byte[]{0, 0, 0, 0, 0, 0, 0});
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

}
