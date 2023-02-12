package technology.dice.dicewhere.building.mmdb;

import java.util.Optional;
import technology.dice.dicewhere.api.api.IP;
import technology.dice.dicewhere.api.api.IpInformation;

public interface LocationSource {
  Optional<IpInformation> resolve(IP ip);
}
