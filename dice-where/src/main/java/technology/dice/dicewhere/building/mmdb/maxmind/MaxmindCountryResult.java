package technology.dice.dicewhere.building.mmdb.maxmind;

import com.maxmind.db.MaxMindDbConstructor;
import com.maxmind.db.MaxMindDbParameter;
import technology.dice.dicewhere.building.mmdb.CountryResult;

public class MaxmindCountryResult implements CountryResult {
  private final MaxmindCountry country;

  @MaxMindDbConstructor
  public MaxmindCountryResult(@MaxMindDbParameter(name = "country") MaxmindCountry country) {
    this.country = country;
  }

  @Override
  public String country() {
    return this.country.getIsoCode();
  }
}
