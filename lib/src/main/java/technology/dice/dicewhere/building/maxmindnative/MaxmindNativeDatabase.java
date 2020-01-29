/*
 * Copyright (C) 2018 - present by Dice Technology Ltd.
 *
 * Please see distribution for license.
 */
package technology.dice.dicewhere.building.maxmindnative;

import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.model.AbstractCountryResponse;
import com.maxmind.geoip2.model.AnonymousIpResponse;
import com.maxmind.geoip2.model.CityResponse;
import com.maxmind.geoip2.model.CountryResponse;
import java.net.InetAddress;
import java.util.Optional;
import technology.dice.dicewhere.api.api.IP;
import technology.dice.dicewhere.api.api.IpInformation;
import technology.dice.dicewhere.api.api.IpInformation.Builder;
import technology.dice.dicewhere.building.IPDatabase;

public class MaxmindNativeDatabase implements IPDatabase {
  private static final String cityType = "GeoLite2-City";
  private static final String countryType = "GeoLite2-Country";
  private final DatabaseReader location;
  private final Optional<DatabaseReader> anonymous;

  public MaxmindNativeDatabase(DatabaseReader location, Optional<DatabaseReader> anonymous) {
    this.location = location;
    this.anonymous = anonymous;
  }

  @Override
  public Optional<IpInformation> get(IP ip) {
    try {
      final InetAddress inetAddress = InetAddress.getByAddress(ip.getBytes());
      final String databaseType = location.getMetadata().getDatabaseType();
      Builder ipInformationBuilder =
          IpInformation.builder().withStartOfRange(ip).withEndOfRange(ip);
      if (cityType.equalsIgnoreCase(databaseType)) {
        final CityResponse cityResponse = location.city(inetAddress);
        if (cityResponse != null) {
          this.populateWithCityResponse(cityResponse, ipInformationBuilder);
        } else {
          return Optional.empty();
        }
      } else if (countryType.equalsIgnoreCase(databaseType)) {
        CountryResponse countryResponse = location.country(inetAddress);
        if (countryResponse != null) {
          this.populateWithCountryResponse(countryResponse, ipInformationBuilder);
        } else {
          return Optional.empty();
        }
      } else {
        throw new RuntimeException("Database type " + databaseType + " not supported.");
      }

      if (anonymous.isPresent()) {
        final AnonymousIpResponse anonymousIpResponse = anonymous.get().anonymousIp(inetAddress);
        if (anonymousIpResponse != null) {
          ipInformationBuilder.isVpn(anonymousIpResponse.isAnonymousVpn());
        }
      }
      return Optional.of(ipInformationBuilder.build());
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
