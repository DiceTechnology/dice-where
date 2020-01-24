package technology.dice.dicewhere.building;

import java.util.Optional;
import technology.dice.dicewhere.api.api.IP;
import technology.dice.dicewhere.api.api.IpInformation;

public interface IPDatabase {
  Optional<IpInformation> get(IP ip);
}
