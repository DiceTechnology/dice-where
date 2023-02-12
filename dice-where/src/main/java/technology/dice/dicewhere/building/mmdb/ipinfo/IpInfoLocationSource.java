package technology.dice.dicewhere.building.mmdb.ipinfo;

import com.google.common.collect.ImmutableSet;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Set;
import technology.dice.dicewhere.building.mmdb.CityResult;
import technology.dice.dicewhere.building.mmdb.CountryResult;
import technology.dice.dicewhere.building.mmdb.MmdbLocationSource;

public class IpInfoLocationSource extends MmdbLocationSource {

  /**
   * Builds a ipinfo database using an mmdb source
   *
   * @param path to the dataset file. ipinfo standard_location.mmdb is supported
   */
  public IpInfoLocationSource(Path path) throws IOException {
    super(path);
  }

  @Override
  public Set<String> supportedCityDatabaseTypes() {
    return ImmutableSet.of("ipinfo standard_location.mmdb");
  }

  @Override
  public Set<String> supportedCountryDatabaseTypes() {
    return ImmutableSet.of();
  }

  public Class<? extends CityResult> cityResult() {
    return IpInfoCityResult.class;
  }

  public Class<? extends CountryResult> countryResult() {
    return IpInfoCountryResult.class;
  }
}
