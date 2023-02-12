package technology.dice.dicewhere.building.mmdb.maxmind;

import com.maxmind.db.MaxMindDbConstructor;
import com.maxmind.db.MaxMindDbParameter;
import java.util.Optional;
import technology.dice.dicewhere.building.mmdb.AnonymousResult;

public class MaxmindAnonymousResult implements AnonymousResult {

  private final boolean vpn;
  private final boolean torExitNode;
  private final boolean hostingProvider;
  private final boolean publicProxy;
  private final boolean residentialProxy;

  @MaxMindDbConstructor
  public MaxmindAnonymousResult(
      @MaxMindDbParameter(name = "is_anonymous_vpn") Boolean vpn,
      @MaxMindDbParameter(name = "is_tor_exit_node") Boolean torExitNode,
      @MaxMindDbParameter(name = "is_residential_proxy") Boolean residentialProxy,
      @MaxMindDbParameter(name = "is_public_proxy") Boolean publicProxy,
      @MaxMindDbParameter(name = "is_hosting_provider") Boolean hostingProvider) {
    this.vpn = Optional.ofNullable(vpn).orElse(false);
    this.torExitNode = Optional.ofNullable(torExitNode).orElse(false);
    this.residentialProxy = Optional.ofNullable(residentialProxy).orElse(false);
    this.publicProxy = Optional.ofNullable(publicProxy).orElse(false);
    this.hostingProvider = Optional.ofNullable(hostingProvider).orElse(false);
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
    return residentialProxy;
  }

  @Override
  public boolean publicProxy() {
    return publicProxy;
  }
}
