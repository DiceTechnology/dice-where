package technology.dice.dicewhere.building.mmdb.maxmind;

import com.maxmind.db.MaxMindDbConstructor;
import com.maxmind.db.MaxMindDbParameter;
import java.util.ArrayList;
import java.util.Map;
import java.util.Optional;
import technology.dice.dicewhere.building.mmdb.CityResult;

public class MaxmindCityResult implements CityResult {
  /*
  This is where it breaks. In a future version we should either return all available names
  or accept a locale definition set at some point in the lifecycle of dice-where
   */
  private static final String LOCALE = "en";
  private final MaxmindCountry country;
  private final MaxmindCityDetails city;
  private final Map<String, String> postal;
  private final ArrayList<MaxmindSubdivisionsDetails> subdivisions;

  @MaxMindDbConstructor
  public MaxmindCityResult(
      @MaxMindDbParameter(name = "country") MaxmindCountry country,
      @MaxMindDbParameter(name = "city") MaxmindCityDetails city,
      @MaxMindDbParameter(name = "postal") Map<String, String> postal,
      @MaxMindDbParameter(name = "subdivisions")
          ArrayList<MaxmindSubdivisionsDetails> subdivisions) {
    this.country = country;
    this.city = city;
    this.postal = postal;
    this.subdivisions = subdivisions;
  }

  @Override
  public String country() {
    return Optional.ofNullable(country).map(c -> c.getIsoCode()).orElse(null);
  }

  @Override
  public String city() {
    return Optional.ofNullable(city).map(c -> c.name(LOCALE)).orElse(null);
  }

  @Override
  public String postal() {
    return Optional.ofNullable(postal).map(p -> p.get("code")).orElse(null);
  }

  @Override
  public String mostSpecificDivision() {
    return (this.subdivisions == null || this.subdivisions.isEmpty())
        ? null
        : this.subdivisions.get(this.subdivisions.size() - 1).name(LOCALE);
  }

  @Override
  public String leastSpecificDivision() {
    return (this.subdivisions == null || this.subdivisions.isEmpty())
        ? null
        : this.subdivisions.get(0).name(LOCALE);
  }

  @Override
  public String geoNameId() {
    return Optional.ofNullable(city).map(c -> c.geoNameId()).orElse(null);
  }
}
