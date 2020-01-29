package technology.dice.dicewhere.building.maxmindnative;

import java.io.IOException;
import java.net.InetAddress;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Optional;
import org.junit.Assert;
import org.junit.Test;
import technology.dice.dicewhere.api.api.IP;
import technology.dice.dicewhere.api.api.IPResolver;
import technology.dice.dicewhere.api.api.IPResolver.Builder;
import technology.dice.dicewhere.api.api.IpInformation;
import technology.dice.dicewhere.provider.ProviderKey;
import technology.dice.dicewhere.provider.maxmind.MaxmindProviderKey;
import technology.dice.dicewhere.reading.maxmind.MaxmindNativeSource;

// As per maxmind T&Cs we are not allowed to re-distribute a Maxmind database. Therefore, the test
// resources necessary to run these tests cannot be uploaded to source control
// Please set the countryDatabase, cityDatabase, and anonymousDatabase variables at the top of this
// class
// and run the tests manually
// The class is suffixed with "IT" so it's skipped by CI
public class MaxmindNativeDatabaseTestIT {
  private static final Path countryDatabase =
      Paths.get(
          "/Users/gluiz/dev/dice-where/localData/GeoLite2-Country_20200128/GeoLite2-Country.mmdb");
  private static final Path cityDatabase =
      Paths.get("/Users/gluiz/dev/dice-where/localData/GeoLite2-City_20200128/GeoLite2-City.mmdb");
  private static final Path anonymousDatabase =
      Paths.get(
          "/Users/gluiz/dev/dice-where/localData/GeoIP2-Anonymous-IP_20200128/GeoIP2-Anonymous-IP.mmdb");

  @Test
  public void lookupWithCountryDatabase() throws IOException {
    final IP ipToLookup = new IP(InetAddress.getByName("8.8.8.8"));
    final IPResolver build =
        new Builder().withProvider(new MaxmindNativeSource(countryDatabase)).build();
    final Map<ProviderKey, Optional<IpInformation>> resolve = build.resolve(ipToLookup);
    Assert.assertEquals(1, resolve.size());
    Assert.assertTrue(resolve.containsKey(MaxmindProviderKey.of()));
    final IpInformation expected =
        IpInformation.builder()
            .withCountryCodeAlpha2("US")
            .withStartOfRange(ipToLookup)
            .withEndOfRange(ipToLookup)
            .build();
    Assert.assertEquals(Optional.of(expected), resolve.get(MaxmindProviderKey.of()));
  }

  @Test
  public void lookupWithCountryDatabaseAndAnonymous() throws IOException {
    final IP ipToLookup = new IP(InetAddress.getByName("8.8.8.8"));
    final IPResolver build =
        new Builder()
            .withProvider(new MaxmindNativeSource(countryDatabase, anonymousDatabase))
            .build();
    final Map<ProviderKey, Optional<IpInformation>> resolve = build.resolve(ipToLookup);
    Assert.assertEquals(1, resolve.size());
    Assert.assertTrue(resolve.containsKey(MaxmindProviderKey.of()));
    final IpInformation expected =
        IpInformation.builder()
            .withCountryCodeAlpha2("US")
            .isVpn(false)
            .withStartOfRange(ipToLookup)
            .withEndOfRange(ipToLookup)
            .build();
    Assert.assertEquals(Optional.of(expected), resolve.get(MaxmindProviderKey.of()));
  }

  @Test
  public void lookupWithCityDatabase() throws IOException {
    final IP ipToLookup = new IP(InetAddress.getByName("193.136.128.169"));
    final IPResolver build =
        new Builder().withProvider(new MaxmindNativeSource(cityDatabase)).build();
    final Map<ProviderKey, Optional<IpInformation>> resolve = build.resolve(ipToLookup);
    Assert.assertEquals(1, resolve.size());
    Assert.assertTrue(resolve.containsKey(MaxmindProviderKey.of()));
    final IpInformation expected =
        IpInformation.builder()
            .withCountryCodeAlpha2("PT")
            .withPostcode("1300-267")
            .withGeonameId("2267057")
            .withCity("Lisbon")
            .withMostSpecificDivision("Lisbon")
            .withLeastSpecificDivision("Lisbon")
            .withStartOfRange(ipToLookup)
            .withEndOfRange(ipToLookup)
            .build();
    Assert.assertEquals(Optional.of(expected), resolve.get(MaxmindProviderKey.of()));
  }

  @Test
  public void lookupWithCityDatabaseAndAnonymous() throws IOException {
    final IP ipToLookup = new IP(InetAddress.getByName("193.136.128.169"));
    final IPResolver build =
        new Builder()
            .withProvider(new MaxmindNativeSource(cityDatabase, anonymousDatabase))
            .build();
    final Map<ProviderKey, Optional<IpInformation>> resolve = build.resolve(ipToLookup);
    Assert.assertEquals(1, resolve.size());
    Assert.assertTrue(resolve.containsKey(MaxmindProviderKey.of()));
    final IpInformation expected =
        IpInformation.builder()
            .withCountryCodeAlpha2("PT")
            .isVpn(false)
            .withPostcode("1300-267")
            .withGeonameId("2267057")
            .withCity("Lisbon")
            .withMostSpecificDivision("Lisbon")
            .withLeastSpecificDivision("Lisbon")
            .withStartOfRange(ipToLookup)
            .withEndOfRange(ipToLookup)
            .build();
    Assert.assertEquals(Optional.of(expected), resolve.get(MaxmindProviderKey.of()));
  }
}
