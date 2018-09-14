package technology.dice.dicewhere.api;

import com.google.common.net.InetAddresses;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.junit.Assert;
import org.junit.Test;
import technology.dice.dicewhere.api.api.IP;
import technology.dice.dicewhere.api.api.IPResolver;
import technology.dice.dicewhere.api.api.IpInformation;
import technology.dice.dicewhere.api.exceptions.DuplicateProviderException;
import technology.dice.dicewhere.api.exceptions.NoProvidersException;
import technology.dice.dicewhere.api.exceptions.ProviderNotAvailableException;
import technology.dice.dicewhere.building.DatabaseBuilderListener;
import technology.dice.dicewhere.lineprocessing.SerializedLine;
import technology.dice.dicewhere.provider.ProviderKey;
import technology.dice.dicewhere.provider.dbip.DbIpProviderKey;
import technology.dice.dicewhere.provider.dbip.reading.DbIpLineReader;
import technology.dice.dicewhere.provider.maxmind.MaxmindProviderKey;
import technology.dice.dicewhere.provider.maxmind.reading.MaxmindDbReader;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Optional;

public class IPResolverTest {

  @Test(expected = DuplicateProviderException.class)
  public void noProviders() throws IOException {
    new IPResolver.Builder()
        .withProvider(
            new DbIpLineReader(
                Paths.get(
                    IPResolverTest.class
                        .getClassLoader()
                        .getResource("provider/dbip/tinyValid.csv")
                        .getFile())))
        .withProvider(
            new DbIpLineReader(
                Paths.get(
                    IPResolverTest.class
                        .getClassLoader()
                        .getResource("provider/dbip/tinyValid.csv")
                        .getFile())))
        .build();
  }

  @Test(expected = ProviderNotAvailableException.class)
  public void resolveWithUnavailableProvider() throws IOException {
    new IPResolver.Builder()
        .withProvider(
            new DbIpLineReader(
                Paths.get(
                    IPResolverTest.class
                        .getClassLoader()
                        .getResource("provider/dbip/tinyValid.csv")
                        .getFile())))
        .build()
        .resolve("1.1.1.1", MaxmindProviderKey.of());
  }

  @Test(expected = NoProvidersException.class)
  public void duplicatedProviders() throws IOException {
    new IPResolver.Builder().build();
  }

  private IPResolver baseResolver() throws IOException {
    return new IPResolver.Builder()
        .withProvider(
            new MaxmindDbReader(
                Paths.get(
                    IPResolverTest.class
                        .getClassLoader()
                        .getResource("provider/maxmind/GeoLite2-City-Locations-en.csv.zip")
                        .getFile()),
                Paths.get(
                    IPResolverTest.class
                        .getClassLoader()
                        .getResource("provider/maxmind/tinyValidV4.csv")
                        .getFile()),
                Paths.get(
                    IPResolverTest.class
                        .getClassLoader()
                        .getResource("provider/maxmind/tinyValidV6.csv")
                        .getFile())))
        .withProvider(
            new DbIpLineReader(
                Paths.get(
                    IPResolverTest.class
                        .getClassLoader()
                        .getResource("provider/dbip/tinyValid.csv")
                        .getFile())))
        .build();
  }

  @Test
  public void existingDualLookupV4() throws IOException {
    IPResolver resolver = baseResolver();
    Map<ProviderKey, Optional<IpInformation>> result = resolver.resolve("1.0.8.17");
    IpInformation expectedBoth =
        new IpInformation(
            "CN",
            "1809858",
            "Guangzhou",
            "Guangdong",
            null,
            null,
            new IP(InetAddresses.forString("1.0.8.0")),
            new IP(InetAddresses.forString("1.0.15.255")),
            null);
    Assert.assertNotNull(result.get(MaxmindProviderKey.of()));
    Assert.assertTrue(result.get(MaxmindProviderKey.of()).isPresent());
    Assert.assertEquals(expectedBoth, result.get(MaxmindProviderKey.of()).get());
    Assert.assertNotNull(result.get(DbIpProviderKey.of()));
    Assert.assertTrue(result.get(DbIpProviderKey.of()).isPresent());
    Assert.assertEquals(expectedBoth, result.get(DbIpProviderKey.of()).get());
  }

  @Test
  public void existingDualLookupV4Async()
      throws IOException, InterruptedException, ExecutionException, TimeoutException {
    IPResolver resolver = baseResolver();
    CompletionStage<Map<ProviderKey, Optional<IpInformation>>> futureResult =
        resolver.resolveAsync("1.0.8.17");
    IpInformation expectedBoth =
        new IpInformation(
            "CN",
            "1809858",
            "Guangzhou",
            "Guangdong",
            null,
            null,
            new IP(InetAddresses.forString("1.0.8.0")),
            new IP(InetAddresses.forString("1.0.15.255")),
            null);

    futureResult.toCompletableFuture().get(1, TimeUnit.SECONDS);

    futureResult
        .toCompletableFuture()
        .thenAccept(
            result -> {
              Assert.assertNotNull(result.get(MaxmindProviderKey.of()));
              Assert.assertTrue(result.get(MaxmindProviderKey.of()).isPresent());
              Assert.assertEquals(expectedBoth, result.get(MaxmindProviderKey.of()).get());
              Assert.assertNotNull(result.get(DbIpProviderKey.of()));
              Assert.assertTrue(result.get(DbIpProviderKey.of()).isPresent());
              Assert.assertEquals(expectedBoth, result.get(DbIpProviderKey.of()).get());
            });
  }

  @Test
  public void existingDualLookupV6() throws IOException {
    IPResolver resolver = baseResolver();
    Map<ProviderKey, Optional<IpInformation>> result =
        resolver.resolve("2001:200:2:ffff:ffff:ffff:ffff:0000");
    IpInformation expectedBoth =
        new IpInformation(
            "JP",
            "1861060",
            null,
            null,
            null,
            null,
            new IP(InetAddresses.forString("2001:200:2::")),
            new IP(InetAddresses.forString("2001:200:3:ffff:ffff:ffff:ffff:ffff")),
            null);
    Assert.assertNotNull(result.get(MaxmindProviderKey.of()));
    Assert.assertTrue(result.get(MaxmindProviderKey.of()).isPresent());
    Assert.assertEquals(expectedBoth, result.get(MaxmindProviderKey.of()).get());
    Assert.assertNotNull(result.get(DbIpProviderKey.of()));
    Assert.assertTrue(result.get(DbIpProviderKey.of()).isPresent());
    Assert.assertEquals(expectedBoth, result.get(DbIpProviderKey.of()).get());
  }

  @Test
  public void justAboveIPV4EndDbIp() throws IOException {
    IPResolver resolver = baseResolver();
    Optional<IpInformation> dbIp = resolver.resolve("217.207.198.104", DbIpProviderKey.of());
    Assert.assertFalse(dbIp.isPresent());
  }

  @Test
  public void justAboveIPV4EndMaxmind() throws IOException {
    IPResolver resolver = baseResolver();
    Optional<IpInformation> maxmind = resolver.resolve("1.0.64.0", MaxmindProviderKey.of());
    Assert.assertFalse(maxmind.isPresent());
  }

  @Test
  public void justAboveIPV6EndDbIp() throws IOException {
    IPResolver resolver = baseResolver();
    Optional<IpInformation> dbIp =
        resolver.resolve("2c0f:fff1:0000:0000:0000:0000:0000:0000", DbIpProviderKey.of());
    Assert.assertFalse(dbIp.isPresent());
  }

  @Test
  public void justAboveIPV6EndMaxmind() throws IOException {
    IPResolver resolver = baseResolver();
    Optional<IpInformation> maxmind =
        resolver.resolve("2001:200:20:0000:0000:0000:0000:0000", MaxmindProviderKey.of());
    Assert.assertFalse(maxmind.isPresent());
  }

  @Test
  public void justBelowPV4EndDbIp() throws IOException {
    IPResolver resolver = baseResolver();
    Optional<IpInformation> dbIp = resolver.resolve("1.0.7.255", DbIpProviderKey.of());
    Assert.assertFalse(dbIp.isPresent());
  }

  @Test
  public void justBelowIPV4EndMaxmind() throws IOException {
    IPResolver resolver = baseResolver();
    Optional<IpInformation> maxmind = resolver.resolve("0.255.255.255", MaxmindProviderKey.of());
    Assert.assertFalse(maxmind.isPresent());
  }

  @Test
  public void justBelowIPV6EndDbIp() throws IOException {
    IPResolver resolver = baseResolver();
    Optional<IpInformation> dbIp =
        resolver.resolve("2001:200:1:ffff:ffff:ffff:ffff:ffff", DbIpProviderKey.of());
    Assert.assertFalse(dbIp.isPresent());
  }

  @Test
  public void justBelowIPV6EndMaxmind() throws IOException {
    IPResolver resolver = baseResolver();
    Optional<IpInformation> maxmind =
        resolver.resolve("600:8801:9400:5a1:948b:ab15:dde3:61a2", MaxmindProviderKey.of());
    Assert.assertFalse(maxmind.isPresent());
  }

  @Test
  public void gapIPV4DbIp() throws IOException {
    IPResolver resolver = baseResolver();
    Optional<IpInformation> dbIp = resolver.resolve("88.55.48.100", DbIpProviderKey.of());
    Assert.assertFalse(dbIp.isPresent());
  }

  @Test
  public void gapIPV4Maxmind() throws IOException {
    IPResolver resolver = baseResolver();
    Optional<IpInformation> dbIp = resolver.resolve("1.0.3.100", MaxmindProviderKey.of());
    Assert.assertFalse(dbIp.isPresent());
  }

  @Test
  public void gapIPV6DbIp() throws IOException {
    IPResolver resolver = baseResolver();
    Optional<IpInformation> dbIp = resolver.resolve("2601:6c24:25ff::", DbIpProviderKey.of());
    Assert.assertFalse(dbIp.isPresent());
  }

  @Test
  public void gapIPV6Maxmind() throws IOException {
    IPResolver resolver = baseResolver();
    Optional<IpInformation> dbIp = resolver.resolve("2001:200:6::", MaxmindProviderKey.of());
    Assert.assertFalse(dbIp.isPresent());
  }

  @Test
  public void ipV6ReadFromIpV4Maxmind() throws IOException {
    IPResolver resolver = baseResolver();
    Map<ProviderKey, Optional<IpInformation>> result = resolver.resolve("0:0:0:0:0:ffff:100:a");
    IpInformation expectedBoth =
        new IpInformation(
            "AU",
            "2065740",
            "Morphett Vale",
            "South Australia",
            null,
            "5162",
            new IP(InetAddresses.forString("1.0.0.0")),
            new IP(InetAddresses.forString("1.0.0.63")),
            null);
    Assert.assertNotNull(result.get(MaxmindProviderKey.of()));
    Assert.assertTrue(result.get(MaxmindProviderKey.of()).isPresent());
    Assert.assertEquals(expectedBoth, result.get(MaxmindProviderKey.of()).get());
    Assert.assertNotNull(result.get(DbIpProviderKey.of()));
    Assert.assertFalse(result.get(DbIpProviderKey.of()).isPresent());
  }

  @Test
  public void ipV6ReadFromIpV4DbIp() throws IOException {
    IPResolver resolver = baseResolver();
    Map<ProviderKey, Optional<IpInformation>> result = resolver.resolve("0:0:0:0:0:ffff:75e3:1b3a");
    IpInformation expectedBoth =
        new IpInformation(
            "IN",
            "1269092",
            "Jeypore",
            "Odisha",
            "Koraput",
            "764002",
            new IP(InetAddresses.forString("117.227.27.58")),
            new IP(InetAddresses.forString("117.227.27.58")),
            null);
    Assert.assertNotNull(result.get(DbIpProviderKey.of()));
    Assert.assertTrue(result.get(DbIpProviderKey.of()).isPresent());
    Assert.assertEquals(expectedBoth, result.get(DbIpProviderKey.of()).get());
    Assert.assertNotNull(result.get(MaxmindProviderKey.of()));
    Assert.assertFalse(result.get(MaxmindProviderKey.of()).isPresent());
  }

  @Test(expected = RuntimeException.class)
  public void outOfOrderDatabaseDbIp() throws IOException {
    new IPResolver.Builder()
        .withProvider(
            new DbIpLineReader(
                Paths.get(
                    IPResolverTest.class
                        .getClassLoader()
                        .getResource("provider/dbip/tinyNotSorted.csv")
                        .getFile())))
        .withBuilderListener(
            new DatabaseBuilderListener() {
              @Override
              public void lineOutOfOrder(
                  ProviderKey provider, SerializedLine serializedLine, Exception e) {
                throw new RuntimeException(e);
              }
            })
        .build();
  }

  @Test
  public void outOfOrderDatabaseDbMaxmindWithIPV6EquivalentToIPV4() throws IOException {
    new IPResolver.Builder()
        .withProvider(
            new MaxmindDbReader(
                Paths.get(
                    IPResolverTest.class
                        .getClassLoader()
                        .getResource("provider/maxmind/GeoLite2-City-Locations-en.csv.zip")
                        .getFile()),
                Paths.get(
                    IPResolverTest.class
                        .getClassLoader()
                        .getResource("provider/maxmind/tinyValidV4.csv")
                        .getFile()),
                Paths.get(
                    IPResolverTest.class
                        .getClassLoader()
                        .getResource("provider/maxmind/containsLowerIpv4V6.csv")
                        .getFile())))
        .withBuilderListener(
            new DatabaseBuilderListener() {
              @Override
              public void lineOutOfOrder(
                  ProviderKey provider, SerializedLine serializedLine, Exception e) {
                throw new RuntimeException(e);
              }
            })
        .build();
  }
}
