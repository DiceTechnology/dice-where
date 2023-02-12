package technology.dice.dicewhere.building.mmdb;

import technology.dice.dicewhere.api.api.IP;
import technology.dice.dicewhere.api.api.IpInformation;

public interface AnonymousSource {
  IpInformation withAnonymousInformation(IP ip, IpInformation ipInformation);
}
