package technology.dice.dicewhere.api.api;

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.Objects;

/**
 * Created by gluiz on 05/07/2018.
 */
public class IP implements Comparable<IP>, Serializable {
	private final byte[] bytes;

	public IP(@NotNull byte[] bytes) {
		Objects.requireNonNull(bytes);
		this.bytes = bytes;
	}

	public IP(@NotNull InetAddress ip) {
		this(Objects.requireNonNull(ip).getAddress());
	}

	public byte[] getBytes() {
		return bytes;
	}

	public boolean isLowerThan(IP other) {
		return this.compareTo(other) < 0;
	}

	public boolean isGreaterThan(IP other) {
		return this.compareTo(other) > 0;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		IP ip = (IP) o;
		return ip.compareTo(this) == 0;
	}

	@Override
	public int hashCode() {
		return Arrays.hashCode(bytes);
	}

	@Override
	public int compareTo(IP o) {
		byte[] myBytes = this.getBytes();
		byte[] theirBytes = o.getBytes();

		// ipv4 before ipv6
		if (myBytes.length < theirBytes.length) return -1;
		if (myBytes.length > theirBytes.length) return 1;

		// compare each byte, most significant first
		for (int i = 0; i < myBytes.length; i++) {
			int myByteAsInt = unsignedByteToInt(myBytes[i]);
			int theirByteAsInt = unsignedByteToInt(theirBytes[i]);
			if (myByteAsInt == theirByteAsInt)
				continue;
			if (myByteAsInt < theirByteAsInt)
				return -1;
			else
				return 1;
		}
		return 0;
	}

	private int unsignedByteToInt(byte b) {
		return (int) b & 0xFF;
	}

}
