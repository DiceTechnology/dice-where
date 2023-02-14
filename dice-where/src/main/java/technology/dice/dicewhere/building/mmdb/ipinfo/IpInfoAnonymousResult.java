package technology.dice.dicewhere.building.mmdb.ipinfo;

import com.maxmind.db.MaxMindDbConstructor;
import com.maxmind.db.MaxMindDbParameter;
import technology.dice.dicewhere.building.mmdb.AnonymousResult;

public class IpInfoAnonymousResult implements AnonymousResult {

  private final boolean vpn;
  private final boolean torExitNode;
  private final boolean hostingProvider;
  private final boolean proxy;
  private final boolean relay;

  @MaxMindDbConstructor
  public IpInfoAnonymousResult(
      @MaxMindDbParameter(name = "vpn") String vpn,
      @MaxMindDbParameter(name = "tor") String torExitNode,
      @MaxMindDbParameter(name = "relay") String relay,
      @MaxMindDbParameter(name = "proxy") String proxy,
      @MaxMindDbParameter(name = "hosting") String hostingProvider) {
    this.vpn = Boolean.parseBoolean(vpn);
    this.torExitNode = Boolean.parseBoolean(torExitNode);
    this.relay = Boolean.parseBoolean(relay);
    this.proxy = Boolean.parseBoolean(proxy);
    this.hostingProvider = Boolean.parseBoolean(hostingProvider);
  }

  @Override
  public boolean hostingProvider() {
    return hostingProvider;
  }

  @Override
  public boolean vpn() {
    return vpn;
  }

  @Override
  public boolean torExitNode() {
    return torExitNode;
  }

  @Override
  public boolean residentialProxy() {
    return relay;
  }

  @Override
  public boolean publicProxy() {
    return proxy;
  }
}
