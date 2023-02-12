package technology.dice.dicewhere.building.ipinfo;

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
import technology.dice.dicewhere.building.mmdb.MmdbDatabase;
import technology.dice.dicewhere.building.mmdb.ipinfo.IpInfoAnonymousSource;
import technology.dice.dicewhere.building.mmdb.ipinfo.IpInfoLocationSource;
import technology.dice.dicewhere.provider.ProviderKey;

// We do not re-destribute IPInfo's datasets. Therefore, the test
// resources necessary to run these tests are not uploaded to source control
// Please set the location and privacy, variables at the top of this
// class
// and run the tests manually
// The class is suffixed with "IT" so it's skipped by CI
public class IpInfoMmdbTestIT {
  private static final Path cityDatabase =
      Paths.get(
          "/Users/gluiz/Downloads/ipinfo/standard_location/mmdb/standard_location-20230210.mmdb");
  private static final Path anonymousDatabase =
      Paths.get("/Users/gluiz/Downloads/ipinfo/privacy/mmdb/privacy_detection_sample.mmdb");

  @Test
  public void lookupWithCityDatabase() throws IOException {
    final IP ipToLookup = new IP(InetAddress.getByName("193.136.128.169"));
    final IPResolver build =
        new Builder()
            .withProvider(new MmdbDatabase("custom", new IpInfoLocationSource(cityDatabase)))
            .build();
    final Map<ProviderKey, Optional<IpInformation>> resolve = build.resolve(ipToLookup);
    assertEquals(1, resolve.size());
    assertTrue(resolve.containsKey(new ProviderKey("custom") {}));
    final IpInformation expected =
        IpInformation.builder()
            .withCountryCodeAlpha2("PT")
            .withPostcode("1000-001")
            .withCityGeonameId("2267057")
            .withCity("Lisbon")
            .withLeastSpecificDivision("Lisbon")
            .withMostSpecificDivision("Lisbon")
            .withStartOfRange(ipToLookup)
            .withEndOfRange(ipToLookup)
            .isVpn(Optional.empty())
            .build();
    assertEquals(Optional.of(expected), resolve.get(new ProviderKey("custom") {}));
  }

  @Test
  public void lookupWithCityDatabaseAndAnonymous() throws IOException {
    final IP ipToLookup = new IP(InetAddress.getByName("110.142.177.68"));
    final IPResolver build =
        new Builder()
            .withProvider(
                new MmdbDatabase(
                    "custom",
                    new IpInfoLocationSource(cityDatabase),
                    new IpInfoAnonymousSource(anonymousDatabase)))
            .build();

    final Map<ProviderKey, Optional<IpInformation>> resolve = build.resolve(ipToLookup);
    assertEquals(1, resolve.size());
    assertTrue(resolve.containsKey(new ProviderKey("custom") {}));
    final IpInformation expected =
        IpInformation.builder()
            .withCountryCodeAlpha2("AU")
            .isVpn(true)
            .withPostcode("3061")
            .withCityGeonameId("2158177")
            .withCity("Melbourne")
            .withLeastSpecificDivision("Victoria")
            .withMostSpecificDivision("Victoria")
            .withStartOfRange(ipToLookup)
            .withEndOfRange(ipToLookup)
            .build();
    assertEquals(Optional.of(expected), resolve.get(new ProviderKey("custom") {}));
  }
}
