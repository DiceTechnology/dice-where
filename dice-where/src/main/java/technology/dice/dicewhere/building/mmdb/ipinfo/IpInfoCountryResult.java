package technology.dice.dicewhere.building.mmdb.ipinfo;

import com.maxmind.db.MaxMindDbConstructor;
import com.maxmind.db.MaxMindDbParameter;
import technology.dice.dicewhere.building.mmdb.CountryResult;

public class IpInfoCountryResult implements CountryResult {

  private String country;

  @MaxMindDbConstructor
  public IpInfoCountryResult(@MaxMindDbParameter(name = "country") String country) {
    this.country = country;
  }

  @Override
  public String country() {
    return this.country;
  }
}
