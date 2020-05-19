/*
 * Copyright (C) 2018 - present by Dice Technology Ltd.
 *
 * Please see distribution for license.
 */
package technology.dice.dicewhere.building.maxmindnative;

import com.google.common.collect.ImmutableSet;
import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.exception.AddressNotFoundException;
import com.maxmind.geoip2.model.AbstractCountryResponse;
import com.maxmind.geoip2.model.AnonymousIpResponse;
import com.maxmind.geoip2.model.CityResponse;
import com.maxmind.geoip2.model.CountryResponse;
import java.net.InetAddress;
import java.util.Optional;
import java.util.Set;
import technology.dice.dicewhere.api.api.IP;
import technology.dice.dicewhere.api.api.IpInformation;
import technology.dice.dicewhere.api.api.IpInformation.Builder;
import technology.dice.dicewhere.building.IPDatabase;

public class MaxmindNativeDatabase implements IPDatabase {
  private static final Set<String> cityTypes = ImmutableSet.of("GeoLite2-City", "GeoIP2-City");
  private static final Set<String> countryTypes =
      ImmutableSet.of("GeoLite2-Country", "GeoIP2-Country");
  private final DatabaseReader location;
  private final Optional<DatabaseReader> anonymous;
  private final NativeDatabaseType nativeDatabaseType;

  /**
   * Builds a maxmind native database
   *
   * @param location a location database. GeoIP2 City and Country are supported
   * @param anonymous an optional GeopIP anonymous database
   */
  public MaxmindNativeDatabase(DatabaseReader location, Optional<DatabaseReader> anonymous) {
    this.location = location;
    this.anonymous = anonymous;
    final String databaseType = location.getMetadata().getDatabaseType();
    if (cityTypes.contains(databaseType)) {
      this.nativeDatabaseType = NativeDatabaseType.CITY;
    } else if (countryTypes.contains(databaseType)) {
      this.nativeDatabaseType = NativeDatabaseType.COUNTRY;
    } else {
      throw new RuntimeException("Database type " + databaseType + " not supported.");
    }
  }

  @Override
  public Optional<IpInformation> get(IP ip) {
    try {
      final InetAddress inetAddress = InetAddress.getByAddress(ip.getBytes());
      Builder ipInformationBuilder =
          IpInformation.builder().withStartOfRange(ip).withEndOfRange(ip);
      if (this.nativeDatabaseType == NativeDatabaseType.CITY) {
        final CityResponse cityResponse = location.city(inetAddress);
        if (cityResponse != null
            && cityResponse.getCountry() != null
            && cityResponse.getCountry().getIsoCode() != null) {
          this.populateWithCityResponse(cityResponse, ipInformationBuilder);
        } else {
          return Optional.empty();
        }
      } else if (this.nativeDatabaseType == NativeDatabaseType.COUNTRY) {
        CountryResponse countryResponse = location.country(inetAddress);
        if (countryResponse != null
            && countryResponse.getCountry() != null
            && countryResponse.getCountry().getIsoCode() != null) {
          this.populateWithCountryResponse(countryResponse, ipInformationBuilder);
        } else {
          return Optional.empty();
        }
      } else {
        throw new RuntimeException(
            "Database type " + this.nativeDatabaseType.name() + " not supported.");
      }

      if (anonymous.isPresent()) {
        final AnonymousIpResponse anonymousIpResponse = anonymous.get().anonymousIp(inetAddress);
        if (anonymousIpResponse != null) {
          ipInformationBuilder.isHostingProvider(anonymousIpResponse.isHostingProvider());
          ipInformationBuilder.isVpn(anonymousIpResponse.isAnonymousVpn());
        }
      }
      return Optional.of(ipInformationBuilder.build());
    } catch (AddressNotFoundException e) {
      return Optional.empty();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private void populateCommon(AbstractCountryResponse baseResponse, Builder ipInformationBuilder) {
    ipInformationBuilder.withCountryCodeAlpha2(baseResponse.getCountry().getIsoCode());
  }

  private void populateWithCityResponse(CityResponse cityResponse, Builder ipInformationBuilder) {
    this.populateCommon(cityResponse, ipInformationBuilder);
    if (cityResponse.getCity() != null) {
      ipInformationBuilder.withCity(cityResponse.getCity().getName());
      ipInformationBuilder.withGeonameId(String.valueOf(cityResponse.getCity().getGeoNameId()));
    }
    if (cityResponse.getLeastSpecificSubdivision() != null) {
      ipInformationBuilder.withLeastSpecificDivision(
          cityResponse.getLeastSpecificSubdivision().getName());
    }

    if (cityResponse.getMostSpecificSubdivision() != null) {
      ipInformationBuilder.withMostSpecificDivision(
          cityResponse.getMostSpecificSubdivision().getName());
    }

    if (cityResponse.getPostal() != null) {
      ipInformationBuilder.withPostcode(cityResponse.getPostal().getCode());
    }
  }

  private void populateWithCountryResponse(CountryResponse country, Builder ipInformationBuilder) {
    this.populateCommon(country, ipInformationBuilder);
  }
}
