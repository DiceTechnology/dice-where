package technology.dice.dicewhere.utils;

import inet.ipaddr.IPAddress;
import inet.ipaddr.IPAddressString;
import technology.dice.dicewhere.api.api.IP;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class IPUtils {

	public static IP increment(IP ip) throws UnknownHostException {
		return increment(ip, 1);
	}

	public static IP decrement(IP ip) throws UnknownHostException {
		return increment(ip, -1);
	}

	public static IP increment(IP ip, int increment) throws UnknownHostException {
		return new IP(from(ip.getBytes()).increment(increment).getBytes());
	}

	public static IPAddress from(IP ip) throws UnknownHostException {
		return from(ip.getBytes());
	}

	public static IPAddress from(byte[] bytes) throws UnknownHostException {
		return new IPAddressString(
				InetAddress.getByAddress(bytes).getHostAddress())
				.getAddress();
	}

}
