/*
 * Copyright (C) 2018 - present by Dice Technology Ltd.
 *
 * Please see distribution for license.
 */

package technology.dice.dicewhere.provider.maxmind.reading;

import inet.ipaddr.IPAddress;
import inet.ipaddr.IPAddressSection;
import technology.dice.dicewhere.api.api.IP;

import javax.annotation.Nonnull;
import java.util.Objects;

public class MaxmindAnonymous {
  private final IP rangeStart;
  private final IP rangeEnd;
  private final boolean isVpn;
  private final boolean isAnonymous;
  private final boolean isPublicProxy;
  private final boolean isHostingProvider;
  private final boolean isTorExitProvider;

  private MaxmindAnonymous(
      @Nonnull IP rangeStart,
      @Nonnull IP rangeEnd,
      boolean isVpn,
      boolean isAnonymous,
      boolean isPublicProxy,
      boolean isHostingProvider,
      boolean isTorExitProvider) {

    this.rangeStart = rangeStart;
    this.rangeEnd = rangeEnd;
    this.isVpn = isVpn;
    this.isAnonymous = isAnonymous;
    this.isPublicProxy = isPublicProxy;
    this.isHostingProvider = isHostingProvider;
    this.isTorExitProvider = isTorExitProvider;
  }

  public IP getRangeStart() {
    return rangeStart;
  }

  public IP getRangeEnd() {
    return rangeEnd;
  }

  public boolean isVpn() {
    return isVpn;
  }

  public boolean isAnonymous() {
    return isAnonymous;
  }

  public boolean isPublicProxy() {
    return isPublicProxy;
  }

  public boolean isHostingProvider() {
    return isHostingProvider;
  }

  public boolean isTorExitProvider() {
    return isTorExitProvider;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    MaxmindAnonymous that = (MaxmindAnonymous) o;
    return isVpn() == that.isVpn()
        && isAnonymous() == that.isAnonymous()
        && isPublicProxy() == that.isPublicProxy()
        && isHostingProvider() == that.isHostingProvider()
        && isTorExitProvider() == that.isTorExitProvider()
        && Objects.equals(getRangeStart(), that.getRangeStart())
        && Objects.equals(getRangeEnd(), that.getRangeEnd());
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        getRangeStart(),
        getRangeEnd(),
        isVpn(),
        isAnonymous(),
        isPublicProxy(),
        isHostingProvider(),
        isTorExitProvider());
  }

  @Override
  public String toString() {
    return "MaxmindAnonymous{"
        + "rangeStart="
        + rangeStart
        + ", rangeEnd="
        + rangeEnd
        + ", isVpn="
        + isVpn
        + ", isAnonymous="
        + isAnonymous
        + ", isPublicProxy="
        + isPublicProxy
        + ", isHostingProvider="
        + isHostingProvider
        + ", isTorExitProvider="
        + isTorExitProvider
        + '}';
  }

  public static Builder builder(@Nonnull IPAddress ipAddress) {
    Objects.requireNonNull(ipAddress);
    IP l = new IP(ipAddress.getLower().getBytes());
    IP u = new IP(ipAddress.toMaxHost().getBytes());
    return new Builder(l, u);
  }

  public static Builder builder(@Nonnull IP rangeStart, @Nonnull IP rangeEnd) {
    return new Builder(Objects.requireNonNull(rangeStart), Objects.requireNonNull(rangeEnd));
  }

  public static class Builder {
    private IP rangeStart;
    private IP rangeEnd;
    private boolean isVpn;
    private boolean isAnonymous;
    private boolean isPublicProxy;
    private boolean isHostingProvider;
    private boolean isTorExitProvider;

    private Builder(@Nonnull IP rangeStart, @Nonnull IP rangeEnd) {
      this.rangeStart = rangeStart;
      this.rangeEnd = rangeEnd;
    }

    public Builder isVpn(boolean isVpn) {
      this.isVpn = isVpn;
      return this;
    }

    public Builder isAnonymous(boolean isAnonymous) {
      this.isAnonymous = isAnonymous;
      return this;
    }

    public Builder isPublicProxy(boolean isPublicProxy) {
      this.isPublicProxy = isPublicProxy;
      return this;
    }

    public Builder isHostingProvider(boolean isHostingProvider) {
      this.isHostingProvider = isHostingProvider;
      return this;
    }

    public Builder isTorExitProvider(boolean isTorExitProvider) {
      this.isTorExitProvider = isTorExitProvider;
      return this;
    }

    public MaxmindAnonymous build() {
      return new MaxmindAnonymous(
          rangeStart,
          rangeEnd,
          isVpn,
          isAnonymous,
          isPublicProxy,
          isHostingProvider,
          isTorExitProvider);
    }
  }
}
