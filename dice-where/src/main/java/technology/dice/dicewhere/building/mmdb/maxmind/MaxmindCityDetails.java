package technology.dice.dicewhere.building.mmdb.maxmind;

import com.maxmind.db.MaxMindDbConstructor;
import com.maxmind.db.MaxMindDbParameter;
import java.util.Map;
import java.util.Optional;

public class MaxmindCityDetails {
  private final Long geonameId;
  private final Map<String, String> names;

  @MaxMindDbConstructor
  public MaxmindCityDetails(
      @MaxMindDbParameter(name = "geoname_id") Long geoNameId,
      @MaxMindDbParameter(name = "names") Map<String, String> names) {
    this.geonameId = geoNameId;
    this.names = names;
  }

  public String name(String locale) {
    return Optional.ofNullable(names.get(locale)).orElse(null);
  }

  public String geoNameId() {
    return String.valueOf(geonameId);
  }
}
