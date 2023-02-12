package technology.dice.dicewhere.building.mmdb.maxmind;

import com.maxmind.db.MaxMindDbConstructor;
import com.maxmind.db.MaxMindDbParameter;

public class MaxmindCountry {
  private final String isoCode;

  @MaxMindDbConstructor
  public MaxmindCountry(@MaxMindDbParameter(name = "iso_code") String isoCode) {
    this.isoCode = isoCode;
  }

  public String getIsoCode() {
    return this.isoCode;
  }
}
