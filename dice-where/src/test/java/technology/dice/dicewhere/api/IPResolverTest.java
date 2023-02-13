/*
 * Copyright (C) 2018 - present by Dice Technology Ltd.
 *
 * Please see distribution for license.
 */

package technology.dice.dicewhere.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.common.net.InetAddresses;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.junit.jupiter.api.Test;
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
import technology.dice.dicewhere.provider.dbip.reading.DbIpLocationAndIspLineReader;
import technology.dice.dicewhere.provider.maxmind.MaxmindProviderKey;
import technology.dice.dicewhere.provider.maxmind.reading.MaxmindDbReader;

public class IPResolverTest {

  @Test
  public void noProviders() {
    assertThrows(
        DuplicateProviderException.class,
        () -> {
          new IPResolver.Builder()
              .withProvider(
                  new DbIpLocationAndIspLineReader(
                      Paths.get(
                          IPResolverTest.class
                              .getClassLoader()
                              .getResource("provider/dbip/tinyValid.csv")
                              .getFile())))
              .withProvider(
                  new DbIpLocationAndIspLineReader(
                      Paths.get(
                          IPResolverTest.class
                              .getClassLoader()
                              .getResource("provider/dbip/tinyValid.csv")
                              .getFile())))
              .build();
        });
  }

  @Test
  public void resolveWithUnavailableProvider() {
    assertThrows(
        ProviderNotAvailableException.class,
        () -> {
          new IPResolver.Builder()
              .withProvider(
                  new DbIpLocationAndIspLineReader(
                      Paths.get(
                          IPResolverTest.class
                              .getClassLoader()
                              .getResource("provider/dbip/tinyValid.csv")
                              .getFile())))
              .build()
              .resolve("1.1.1.1", MaxmindProviderKey.of());
        });
  }

  @Test
  public void duplicatedProviders() {
    assertThrows(NoProvidersException.class, () -> new IPResolver.Builder().build());
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
            new DbIpLocationAndIspLineReader(
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
        IpInformation.builder()
            .withCountryCodeAlpha2("CN")
            .withGeonameId("1809858")
            .withCity("Guangzhou")
            .withLeastSpecificDivision("Guangdong")
            .withStartOfRange(new IP(InetAddresses.forString("1.0.8.0")))
            .withEndOfRange(new IP(InetAddresses.forString("1.0.15.255")))
            .build();
    assertNotNull(result.get(MaxmindProviderKey.of()));
    assertTrue(result.get(MaxmindProviderKey.of()).isPresent());
    assertEquals(expectedBoth, result.get(MaxmindProviderKey.of()).get());
    assertNotNull(result.get(DbIpProviderKey.of()));
    assertTrue(result.get(DbIpProviderKey.of()).isPresent());
    assertEquals(expectedBoth, result.get(DbIpProviderKey.of()).get());
  }

  @Test
  public void existingDualLookupV4Async()
      throws IOException, InterruptedException, ExecutionException, TimeoutException {
    IPResolver resolver = baseResolver();
    CompletionStage<Map<ProviderKey, Optional<IpInformation>>> futureResult =
        resolver.resolveAsync("1.0.8.17");
    IpInformation expectedBoth =
        IpInformation.builder()
            .withCountryCodeAlpha2("CN")
            .withGeonameId("1809858")
            .withCity("Guangzhou")
            .withLeastSpecificDivision("Guangdong")
            .withStartOfRange(new IP(InetAddresses.forString("1.0.8.0")))
            .withEndOfRange(new IP(InetAddresses.forString("1.0.15.255")))
            .build();

    futureResult.toCompletableFuture().get(1, TimeUnit.SECONDS);

    futureResult
        .toCompletableFuture()
        .thenAccept(
            result -> {
              assertNotNull(result.get(MaxmindProviderKey.of()));
              assertTrue(result.get(MaxmindProviderKey.of()).isPresent());
              assertEquals(expectedBoth, result.get(MaxmindProviderKey.of()).get());
              assertNotNull(result.get(DbIpProviderKey.of()));
              assertTrue(result.get(DbIpProviderKey.of()).isPresent());
              assertEquals(expectedBoth, result.get(DbIpProviderKey.of()).get());
            });
  }

  @Test
  public void existingDualLookupV6() throws IOException {
    IPResolver resolver = baseResolver();
    Map<ProviderKey, Optional<IpInformation>> result =
        resolver.resolve("2001:200:2:ffff:ffff:ffff:ffff:0000");
    IpInformation expectedBoth =
        IpInformation.builder()
            .withCountryCodeAlpha2("JP")
            .withGeonameId("1861060")
            .withStartOfRange(new IP(InetAddresses.forString("2001:200:2::")))
            .withEndOfRange(new IP(InetAddresses.forString("2001:200:3:ffff:ffff:ffff:ffff:ffff")))
            .build();
    assertNotNull(result.get(MaxmindProviderKey.of()));
    assertTrue(result.get(MaxmindProviderKey.of()).isPresent());
    assertEquals(expectedBoth, result.get(MaxmindProviderKey.of()).get());
    assertNotNull(result.get(DbIpProviderKey.of()));
    assertTrue(result.get(DbIpProviderKey.of()).isPresent());
    assertEquals(expectedBoth, result.get(DbIpProviderKey.of()).get());
  }

  @Test
  public void justAboveIPV4EndDbIp() throws IOException {
    IPResolver resolver = baseResolver();
    Optional<IpInformation> dbIp = resolver.resolve("217.207.198.104", DbIpProviderKey.of());
    assertFalse(dbIp.isPresent());
  }

  @Test
  public void justAboveIPV4EndMaxmind() throws IOException {
    IPResolver resolver = baseResolver();
    Optional<IpInformation> maxmind = resolver.resolve("1.0.64.0", MaxmindProviderKey.of());
    assertFalse(maxmind.isPresent());
  }

  @Test
  public void justAboveIPV6EndDbIp() throws IOException {
    IPResolver resolver = baseResolver();
    Optional<IpInformation> dbIp =
        resolver.resolve("2c0f:fff1:0000:0000:0000:0000:0000:0000", DbIpProviderKey.of());
    assertFalse(dbIp.isPresent());
  }

  @Test
  public void justAboveIPV6EndMaxmind() throws IOException {
    IPResolver resolver = baseResolver();
    Optional<IpInformation> maxmind =
        resolver.resolve("2001:200:20:0000:0000:0000:0000:0000", MaxmindProviderKey.of());
    assertFalse(maxmind.isPresent());
  }

  @Test
  public void justBelowPV4EndDbIp() throws IOException {
    IPResolver resolver = baseResolver();
    Optional<IpInformation> dbIp = resolver.resolve("1.0.7.255", DbIpProviderKey.of());
    assertFalse(dbIp.isPresent());
  }

  @Test
  public void justBelowIPV4EndMaxmind() throws IOException {
    IPResolver resolver = baseResolver();
    Optional<IpInformation> maxmind = resolver.resolve("0.255.255.255", MaxmindProviderKey.of());
    assertFalse(maxmind.isPresent());
  }

  @Test
  public void justBelowIPV6EndDbIp() throws IOException {
    IPResolver resolver = baseResolver();
    Optional<IpInformation> dbIp =
        resolver.resolve("2001:200:1:ffff:ffff:ffff:ffff:ffff", DbIpProviderKey.of());
    assertFalse(dbIp.isPresent());
  }

  @Test
  public void justBelowIPV6EndMaxmind() throws IOException {
    IPResolver resolver = baseResolver();
    Optional<IpInformation> maxmind =
        resolver.resolve("600:8801:9400:5a1:948b:ab15:dde3:61a2", MaxmindProviderKey.of());
    assertFalse(maxmind.isPresent());
  }

  @Test
  public void gapIPV4DbIp() throws IOException {
    IPResolver resolver = baseResolver();
    Optional<IpInformation> dbIp = resolver.resolve("88.55.48.100", DbIpProviderKey.of());
    assertFalse(dbIp.isPresent());
  }

  @Test
  public void gapIPV4Maxmind() throws IOException {
    IPResolver resolver = baseResolver();
    Optional<IpInformation> dbIp = resolver.resolve("1.0.3.100", MaxmindProviderKey.of());
    assertFalse(dbIp.isPresent());
  }

  @Test
  public void gapIPV6DbIp() throws IOException {
    IPResolver resolver = baseResolver();
    Optional<IpInformation> dbIp = resolver.resolve("2601:6c24:25ff::", DbIpProviderKey.of());
    assertFalse(dbIp.isPresent());
  }

  @Test
  public void gapIPV6Maxmind() throws IOException {
    IPResolver resolver = baseResolver();
    Optional<IpInformation> dbIp = resolver.resolve("2001:200:6::", MaxmindProviderKey.of());
    assertFalse(dbIp.isPresent());
  }

  @Test
  public void ipV6ReadFromIpV4Maxmind() throws IOException {
    IPResolver resolver = baseResolver();
    Map<ProviderKey, Optional<IpInformation>> result = resolver.resolve("0:0:0:0:0:ffff:100:a");
    IpInformation expectedBoth =
        IpInformation.builder()
            .withCountryCodeAlpha2("AU")
            .withGeonameId("2065740")
            .withCity("Morphett Vale")
            .withLeastSpecificDivision("South Australia")
            .withPostcode("5162")
            .withStartOfRange(new IP(InetAddresses.forString("1.0.0.0")))
            .withEndOfRange(new IP(InetAddresses.forString("1.0.0.63")))
            .build();
    assertNotNull(result.get(MaxmindProviderKey.of()));
    assertTrue(result.get(MaxmindProviderKey.of()).isPresent());
    assertEquals(expectedBoth, result.get(MaxmindProviderKey.of()).get());
    assertNotNull(result.get(DbIpProviderKey.of()));
    assertFalse(result.get(DbIpProviderKey.of()).isPresent());
  }

  @Test
  public void ipV6ReadFromIpV4DbIp() throws IOException {
    IPResolver resolver = baseResolver();
    Map<ProviderKey, Optional<IpInformation>> result = resolver.resolve("0:0:0:0:0:ffff:75e3:1b3a");
    IpInformation expectedBoth =
        IpInformation.builder()
            .withCountryCodeAlpha2("IN")
            .withGeonameId("1269092")
            .withCity("Jeypore")
            .withLeastSpecificDivision("Odisha")
            .withMostSpecificDivision("Koraput")
            .withPostcode("764002")
            .withStartOfRange(new IP(InetAddresses.forString("117.227.27.58")))
            .withEndOfRange(new IP(InetAddresses.forString("117.227.27.58")))
            .build();
    assertNotNull(result.get(DbIpProviderKey.of()));
    assertTrue(result.get(DbIpProviderKey.of()).isPresent());
    assertEquals(expectedBoth, result.get(DbIpProviderKey.of()).get());
    assertNotNull(result.get(MaxmindProviderKey.of()));
    assertFalse(result.get(MaxmindProviderKey.of()).isPresent());
  }

  @Test
  public void outOfOrderDatabaseDbIp() {
    assertThrows(
        RuntimeException.class,
        () -> {
          new IPResolver.Builder()
              .withProvider(
                  new DbIpLocationAndIspLineReader(
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
        });
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
