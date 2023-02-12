package technology.dice.dicewhere.building.mmdb.maxmind;

import com.maxmind.db.Reader;
import java.util.Optional;
import technology.dice.dicewhere.building.mmdb.AnonymousResult;
import technology.dice.dicewhere.building.mmdb.CityResult;
import technology.dice.dicewhere.building.mmdb.CountryResult;
import technology.dice.dicewhere.building.mmdb.MmdbDatabase;

public class MaxmindMmdbDatabase extends MmdbDatabase {

  /**
   * Builds a maxmind database using the mmdb format
   *
   * @param location a location database. GeoIP2 City and Country are supported
   * @param anonymous an optional GeopIP2 anonymous database
   */
  public MaxmindMmdbDatabase(Reader location, Optional<Reader> anonymous) {
    super(null, null);
  }

  public Class<? extends CityResult> cityResult() {
    return MaxmindCityResult.class;
  }

  public Class<? extends CountryResult> countryResult() {
    return MaxmindCountryResult.class;
  }

  public Class<? extends AnonymousResult> anonymousResult() {
    return MaxmindAnonymousResult.class;
  }
}
