package technology.dice.dicewhere.utils;

import inet.ipaddr.IPAddress;
import inet.ipaddr.IPAddressString;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class IPUtils {

	public static IPAddress from(byte[] bytes) throws UnknownHostException {
		return new IPAddressString(
				InetAddress.getByAddress(bytes).getCanonicalHostName())
				.getAddress();
	}

}
