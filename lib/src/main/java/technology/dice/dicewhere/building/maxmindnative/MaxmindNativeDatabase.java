package technology.dice.dicewhere.building.maxmindnative;

import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.model.AnonymousIpResponse;
import com.maxmind.geoip2.model.CityResponse;
import java.net.InetAddress;
import java.util.Optional;
import technology.dice.dicewhere.api.api.IP;
import technology.dice.dicewhere.api.api.IpInformation;
import technology.dice.dicewhere.api.api.IpInformation.Builder;
import technology.dice.dicewhere.building.IPDatabase;

public class MaxmindNativeDatabase implements IPDatabase {
  private final DatabaseReader city;
  private final Optional<DatabaseReader> anonymous;

  public MaxmindNativeDatabase(DatabaseReader city, Optional<DatabaseReader> anonymous) {
    this.city = city;
    this.anonymous = anonymous;
  }

  @Override
  public Optional<IpInformation> get(IP ip) {
    try {
      final InetAddress inetAddress = InetAddress.getByAddress(ip.getBytes());
      final CityResponse cityResponse = city.city(inetAddress);
      if (cityResponse == null) {
        return Optional.empty();
      }
      Builder ipInformationBuilder =
          IpInformation.builder().withStartOfRange(ip).withEndOfRange(ip);
      ipInformationBuilder
          .withCountryCodeAlpha2(cityResponse.getCountry().getIsoCode())
          .withOriginalLine(cityResponse.toJson());

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
}
