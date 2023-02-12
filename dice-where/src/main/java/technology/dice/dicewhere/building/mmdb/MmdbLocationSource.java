package technology.dice.dicewhere.building.mmdb;

import com.maxmind.db.CHMCache;
import com.maxmind.db.Reader;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.nio.file.Path;
import java.util.Optional;
import java.util.Set;
import technology.dice.dicewhere.api.api.IP;
import technology.dice.dicewhere.api.api.IpInformation;
import technology.dice.dicewhere.api.api.IpInformation.Builder;

public abstract class MmdbLocationSource implements LocationSource {

  private final Reader location;
  protected final MmdbDatabaseType mmdbDatabaseType;

  public MmdbLocationSource(Path path) throws IOException {
    this.location = new Reader(new File(path.toFile().toURI()), new CHMCache());
    final String databaseType = location.getMetadata().getDatabaseType();
    if (supportedCityDatabaseTypes().contains(databaseType)) {
      this.mmdbDatabaseType = MmdbDatabaseType.CITY;
    } else if (supportedCountryDatabaseTypes().contains(databaseType)) {
      this.mmdbDatabaseType = MmdbDatabaseType.COUNTRY;
    } else {
      throw new RuntimeException("Database type " + databaseType + " not supported.");
    }
  }

  public abstract Set<String> supportedCityDatabaseTypes();

  public abstract Set<String> supportedCountryDatabaseTypes();

  public abstract Class<? extends CityResult> cityResult();

  public abstract Class<? extends CountryResult> countryResult();

  @Override
  public Optional<IpInformation> resolve(IP ip) {
    try {
      final InetAddress inetAddress = InetAddress.getByAddress(ip.getBytes());
      Builder ipInformationBuilder =
          IpInformation.builder().withStartOfRange(ip).withEndOfRange(ip);
      if (this.mmdbDatabaseType == MmdbDatabaseType.CITY) {
        CityResult cityResponse = location.get(inetAddress, cityResult());

        if (cityResponse != null && cityResponse.country() != null) {
          this.populateWithCityResponse(cityResponse, ipInformationBuilder);
        } else {
          return Optional.empty();
        }
      } else if (this.mmdbDatabaseType == MmdbDatabaseType.COUNTRY) {
        CountryResult countryResponse = location.get(inetAddress, countryResult());
        if (countryResponse != null && countryResponse.country() != null) {
          this.populateWithCountryResponse(countryResponse, ipInformationBuilder);
        } else {
          return Optional.empty();
        }
      } else {
        throw new RuntimeException(
            "Database type " + this.mmdbDatabaseType.name() + " not supported.");
      }
      return Optional.of(ipInformationBuilder.build());
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private void populateWithCityResponse(CityResult cityResponse, Builder ipInformationBuilder) {
    ipInformationBuilder.withCountryCodeAlpha2(cityResponse.country());
    if (cityResponse.city() != null) {
      ipInformationBuilder.withCity(cityResponse.city());
      ipInformationBuilder.withCityGeonameId(String.valueOf(cityResponse.geoNameId()));
    }
    if (cityResponse.leastSpecificDivision() != null) {
      ipInformationBuilder.withLeastSpecificDivision(cityResponse.leastSpecificDivision());
    }

    if (cityResponse.mostSpecificDivision() != null) {
      ipInformationBuilder.withMostSpecificDivision(cityResponse.mostSpecificDivision());
    }

    if (cityResponse.postal() != null) {
      ipInformationBuilder.withPostcode(cityResponse.postal());
    }
  }

  private void populateWithCountryResponse(
      CountryResult countryResponse, Builder ipInformationBuilder) {
    ipInformationBuilder.withCountryCodeAlpha2(countryResponse.country());
  }
}
