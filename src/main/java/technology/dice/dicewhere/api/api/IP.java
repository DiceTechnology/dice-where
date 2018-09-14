/*
 * Copyright (C) 2018 - present by Dice Technology Ltd.
 *
 * Please see distribution for license.
 */

package technology.dice.dicewhere.api.api;

import java.io.Serializable;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.Objects;
import javax.annotation.Nonnull;

public class IP implements Comparable<IP>, Serializable {
  private final byte[] bytes;

  public IP(@Nonnull byte[] bytes) {
    Objects.requireNonNull(bytes);
    this.bytes = bytes;
  }

  public IP(@Nonnull InetAddress ip) {
    this(Objects.requireNonNull(ip).getAddress());
  }

  public byte[] getBytes() {
    return bytes;
  }

  public boolean isLowerThan(IP other) {
    return compareTo(other) < 0;
  }

  public boolean isGreaterThan(IP other) {
    return compareTo(other) > 0;
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
  public int compareTo(@Nonnull IP other) {
    Objects.requireNonNull(other);
    byte[] myBytes = getBytes();
    byte[] theirBytes = other.getBytes();

    // ipv4 before ipv6
    if (myBytes.length < theirBytes.length) return -1;
    if (myBytes.length > theirBytes.length) return 1;

    // compare each byte, most significant first
    for (int i = 0; i < myBytes.length; i++) {
      int myByteAsInt = unsignedByteToInt(myBytes[i]);
      int theirByteAsInt = unsignedByteToInt(theirBytes[i]);
      if (myByteAsInt == theirByteAsInt) continue;
      if (myByteAsInt < theirByteAsInt) return -1;
      else return 1;
    }
    return 0;
  }

  private int unsignedByteToInt(byte b) {
    return (int) b & 0xFF;
  }
}
