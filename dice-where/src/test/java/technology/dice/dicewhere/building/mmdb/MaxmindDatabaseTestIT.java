package technology.dice.dicewhere.building.mmdb;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.net.InetAddress;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import technology.dice.dicewhere.api.api.IP;
import technology.dice.dicewhere.api.api.IPResolver;
import technology.dice.dicewhere.api.api.IPResolver.Builder;
import technology.dice.dicewhere.api.api.IpInformation;
import technology.dice.dicewhere.building.mmdb.maxmind.MaxmindGeoIp2Source;
import technology.dice.dicewhere.building.mmdb.maxmind.MaxmindGeopIp2dAnonymousSource;
import technology.dice.dicewhere.provider.ProviderKey;
import technology.dice.dicewhere.provider.maxmind.MaxmindProviderKey;

// As per maxmind T&Cs we are not allowed to re-distribute a Maxmind database. Therefore, the test
// resources necessary to run these tests cannot be uploaded to source control
// Please set the countryDatabase, cityDatabase, and anonymousDatabase variables at the top of this
// class
// and run the tests manually
// The class is suffixed with "IT" so it's skipped by CI
public class MaxmindDatabaseTestIT {
  private static final Path countryDatabase =
      Paths.get(
          "/Users/gluiz/dev/dice-where/localData/GeoLite2-Country_20200128/GeoLite2-Country.mmdb");
  private static final Path cityDatabase =
      Paths.get("/Users/gluiz/dev/dice-where/localData/GeoLite2-City_20200128/GeoLite2-City.mmdb");
  private static final Path anonymousDatabase =
      Paths.get("/Users/gluiz/Downloads/maxmind/GeoIP2/Anonymous/GeoIP2-Anonymous-IP.mmdb");

  @Test
  public void lookupWithCountryDatabase() throws IOException {
    final IP ipToLookup = new IP(InetAddress.getByName("8.8.8.8"));
    final IPResolver build =
        new Builder()
            .withProvider(
                new MmdbDatabase(
                    MaxmindProviderKey.of().name(), new MaxmindGeoIp2Source(countryDatabase)))
            .build();
    final Map<ProviderKey, Optional<IpInformation>> resolve = build.resolve(ipToLookup);
    assertEquals(1, resolve.size());
    assertTrue(resolve.containsKey(MaxmindProviderKey.of()));
    final IpInformation expected =
        IpInformation.builder()
            .withCountryCodeAlpha2("US")
            .withStartOfRange(ipToLookup)
            .withEndOfRange(ipToLookup)
            .isVpn(Optional.empty())
            .build();
    assertEquals(Optional.of(expected), resolve.get(MaxmindProviderKey.of()));
  }

  @Test
  public void lookupWithCountryDatabaseAndAnonymous() throws IOException {
    final IP ipToLookup = new IP(InetAddress.getByName("8.8.8.8"));
    final IPResolver build =
        new Builder()
            .withProvider(
                new MmdbDatabase(
                    MaxmindProviderKey.of().name(),
                    new MaxmindGeoIp2Source(countryDatabase),
                    new MaxmindGeopIp2dAnonymousSource(anonymousDatabase)))
            .build();
    final Map<ProviderKey, Optional<IpInformation>> resolve = build.resolve(ipToLookup);
    assertEquals(1, resolve.size());
    assertTrue(resolve.containsKey(MaxmindProviderKey.of()));
    final IpInformation expected =
        IpInformation.builder()
            .withCountryCodeAlpha2("US")
            .isVpn(false)
            .withStartOfRange(ipToLookup)
            .withEndOfRange(ipToLookup)
            .build();
    assertEquals(Optional.of(expected), resolve.get(MaxmindProviderKey.of()));
  }

  @Test
  public void lookupWithCityDatabase() throws IOException {
    final IP ipToLookup = new IP(InetAddress.getByName("193.136.128.169"));
    final IPResolver build =
        new Builder()
            .withProvider(
                new MmdbDatabase(
                    MaxmindProviderKey.of().name(), new MaxmindGeoIp2Source(cityDatabase)))
            .build();
    final Map<ProviderKey, Optional<IpInformation>> resolve = build.resolve(ipToLookup);
    assertEquals(1, resolve.size());
    assertTrue(resolve.containsKey(MaxmindProviderKey.of()));
    final IpInformation expected =
        IpInformation.builder()
            .withCountryCodeAlpha2("PT")
            .withPostcode("1300-267")
            .withCityGeonameId("2267057")
            .withCity("Lisbon")
            .isVpn(Optional.empty())
            .withMostSpecificDivision("Lisbon")
            .withLeastSpecificDivision("Lisbon")
            .withStartOfRange(ipToLookup)
            .withEndOfRange(ipToLookup)
            .build();
    assertEquals(Optional.of(expected), resolve.get(MaxmindProviderKey.of()));
  }

  @Test
  public void lookupWithCityDatabaseAndAnonymous() throws IOException {
    final IP ipToLookup = new IP(InetAddress.getByName("193.136.128.169"));
    final IPResolver build =
        new Builder()
            .withProvider(
                new MmdbDatabase(
                    MaxmindProviderKey.of().name(),
                    new MaxmindGeoIp2Source(cityDatabase),
                    new MaxmindGeopIp2dAnonymousSource(anonymousDatabase)))
            .build();
    final Map<ProviderKey, Optional<IpInformation>> resolve = build.resolve(ipToLookup);
    assertEquals(1, resolve.size());
    assertTrue(resolve.containsKey(MaxmindProviderKey.of()));
    final IpInformation expected =
        IpInformation.builder()
            .withCountryCodeAlpha2("PT")
            .isVpn(false)
            .withPostcode("1300-267")
            .withCityGeonameId("2267057")
            .withCity("Lisbon")
            .withMostSpecificDivision("Lisbon")
            .withLeastSpecificDivision("Lisbon")
            .withStartOfRange(ipToLookup)
            .withEndOfRange(ipToLookup)
            .build();
    assertEquals(Optional.of(expected), resolve.get(MaxmindProviderKey.of()));
  }
}
