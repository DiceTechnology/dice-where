package technology.dice.dicewhere.provider.maxmind.reading;

import inet.ipaddr.IPAddress;
import technology.dice.dicewhere.api.api.IP;

import javax.annotation.Nonnull;
import java.util.Objects;

public class MaxmindAnonymous {
  private final IPAddress ipAddressRange;
  private final boolean isVpn;
  private final boolean isAnonymous;
  private final boolean isPublicProxy;
  private final boolean isHostingProvider;
  private final boolean isTorExitProvider;

  private MaxmindAnonymous(
      @Nonnull IPAddress ipAddressRange,
      boolean isVpn,
      boolean isAnonymous,
      boolean isPublicProxy,
      boolean isHostingProvider,
      boolean isTorExitProvider) {

    this.ipAddressRange = ipAddressRange;
    this.isVpn = isVpn;
    this.isAnonymous = isAnonymous;
    this.isPublicProxy = isPublicProxy;
    this.isHostingProvider = isHostingProvider;
    this.isTorExitProvider = isTorExitProvider;
  }

  public IPAddress getIpAddressRange() {
    return ipAddressRange;
  }

  public IP getLowerBound() {
    return new IP(ipAddressRange.getLower().getBytes());
  }

  public IP getUpperBound() {
    return new IP(ipAddressRange.getUpper().getBytes());
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

  public static Builder builder(@Nonnull IPAddress ipAddress) {
    return new Builder(Objects.requireNonNull(ipAddress));
  }

  public static class Builder {
    private IPAddress ipAddressRange;
    private boolean isVpn;
    private boolean isAnonymous;
    private boolean isPublicProxy;
    private boolean isHostingProvider;
    private boolean isTorExitProvider;


    private Builder(@Nonnull IPAddress ipAddress) {
      this.ipAddressRange = ipAddress;
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
          ipAddressRange,
          isVpn,
          isAnonymous,
          isPublicProxy,
          isHostingProvider,
          isTorExitProvider);
    }
  }
}
