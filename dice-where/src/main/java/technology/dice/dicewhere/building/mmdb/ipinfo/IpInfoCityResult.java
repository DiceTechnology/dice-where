package technology.dice.dicewhere.building.mmdb.ipinfo;

import com.maxmind.db.MaxMindDbConstructor;
import com.maxmind.db.MaxMindDbParameter;
import technology.dice.dicewhere.building.mmdb.CityResult;

public class IpInfoCityResult implements CityResult {
  private final String country;
  private final String city;
  private final String postal;
  private final String region;
  private final String geonameId;

  @MaxMindDbConstructor
  public IpInfoCityResult(
      @MaxMindDbParameter(name = "country") String country,
      @MaxMindDbParameter(name = "city") String city,
      @MaxMindDbParameter(name = "postal_code") String postal,
      @MaxMindDbParameter(name = "region") String region,
      @MaxMindDbParameter(name = "geoname_id") String geonameId) {
    this.country = country;
    this.city = city;
    this.postal = postal;
    this.region = region;
    this.geonameId = geonameId;
  }

  @Override
  public String country() {
    return country;
  }

  @Override
  public String city() {
    return city;
  }

  @Override
  public String postal() {
    return postal;
  }

  @Override
  public String mostSpecificDivision() {
    return region;
  }

  @Override
  public String leastSpecificDivision() {
    return region;
  }

  @Override
  public String geoNameId() {
    return geonameId;
  }
}
