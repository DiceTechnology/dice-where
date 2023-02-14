package technology.dice.dicewhere.building.mmdb.maxmind;

import com.google.common.collect.ImmutableSet;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Set;
import technology.dice.dicewhere.building.mmdb.CityResult;
import technology.dice.dicewhere.building.mmdb.CountryResult;
import technology.dice.dicewhere.building.mmdb.MmdbLocationSource;

public class MaxmindGeoIp2Source extends MmdbLocationSource {

  /**
   * Builds a maxmind GeoIp2 database using an mmdb source
   *
   * @param path to the dataset file. GeoLite2-City, GeoIP2-City,GeoLite2-Country, GeoIP2-Country
   *     are supported
   */
  public MaxmindGeoIp2Source(Path path) {
    super(path);
  }

  @Override
  public Set<String> supportedCityDatabaseTypes() {
    return ImmutableSet.of("GeoLite2-City", "GeoIP2-City");
  }

  @Override
  public Set<String> supportedCountryDatabaseTypes() {
    return ImmutableSet.of("GeoLite2-Country", "GeoIP2-Country");
  }

  @Override
  public Class<? extends CityResult> cityResult() {
    return MaxmindCityResult.class;
  }

  @Override
  public Class<? extends CountryResult> countryResult() {
    return MaxmindCountryResult.class;
  }
}
