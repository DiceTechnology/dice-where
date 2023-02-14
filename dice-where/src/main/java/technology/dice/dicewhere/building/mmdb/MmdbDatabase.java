/*
 * Copyright (C) 2018 - present by Dice Technology Ltd.
 *
 * Please see distribution for license.
 */
package technology.dice.dicewhere.building.mmdb;

import java.util.Optional;
import technology.dice.dicewhere.api.api.IP;
import technology.dice.dicewhere.api.api.IpInformation;
import technology.dice.dicewhere.building.DatabaseBuilderListener;
import technology.dice.dicewhere.building.IPDatabase;
import technology.dice.dicewhere.lineprocessing.LineProcessorListener;
import technology.dice.dicewhere.provider.ProviderKey;
import technology.dice.dicewhere.reading.LineReaderListener;
import technology.dice.dicewhere.reading.SourceReader;

public class MmdbDatabase implements IPDatabase, SourceReader {

  private final LocationSource locationSource;
  private final Optional<AnonymousSource> anonymousSource;
  private final ProviderKey key;

  public MmdbDatabase(
      String name, LocationSource locationSource, Optional<AnonymousSource> anonymousSource) {
    this.locationSource = locationSource;
    this.anonymousSource = anonymousSource;
    this.key = new ProviderKey(name) {};
  }

  public MmdbDatabase(String name, LocationSource locationSource, AnonymousSource anonymousSource) {
    this.locationSource = locationSource;
    this.anonymousSource = Optional.of(anonymousSource);
    this.key = new ProviderKey(name) {};
  }

  public MmdbDatabase(String name, LocationSource locationSource) {
    this(name, locationSource, Optional.empty());
  }

  @Override
  public Optional<IpInformation> get(IP ip) {
    return locationSource
        .resolve(ip)
        .map(info -> anonymousSource.map(a -> a.withAnonymousInformation(ip, info)).orElse(info));
  }

  @Override
  public IPDatabase read(
      boolean retainOriginalLine,
      LineReaderListener readerListener,
      LineProcessorListener processListener,
      DatabaseBuilderListener buildingListener,
      int workersCount) {
    return this;
  }

  @Override
  public ProviderKey provider() {
    return this.key;
  }
}
