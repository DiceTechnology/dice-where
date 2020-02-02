/*
 * Copyright (C) 2018 - present by Dice Technology Ltd.
 *
 * Please see distribution for license.
 */
package technology.dice.dicewhere.reading.maxmind;

import com.maxmind.db.CHMCache;
import com.maxmind.geoip2.DatabaseReader;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;
import technology.dice.dicewhere.building.DatabaseBuilderListener;
import technology.dice.dicewhere.building.IPDatabase;
import technology.dice.dicewhere.building.maxmindnative.MaxmindNativeDatabase;
import technology.dice.dicewhere.lineprocessing.LineProcessorListener;
import technology.dice.dicewhere.provider.ProviderKey;
import technology.dice.dicewhere.provider.maxmind.MaxmindProviderKey;
import technology.dice.dicewhere.reading.LineReaderListener;
import technology.dice.dicewhere.reading.SourceReader;

public class MaxmindNativeSource implements SourceReader {

  private final DatabaseReader location;
  private final Optional<DatabaseReader> anonymous;

  public MaxmindNativeSource(Path location, Path anonymous) throws IOException {
    this(location, Optional.of(anonymous));
  }

  public MaxmindNativeSource(Path location) throws IOException {
    this(location, Optional.empty());
  }

  public MaxmindNativeSource(Path location, Optional<Path> anonymous) throws IOException {
    this.location = new DatabaseReader.Builder(location.toFile()).withCache(new CHMCache()).build();
    if (anonymous.isPresent()) {
      this.anonymous =
          Optional.of(
              new DatabaseReader.Builder(anonymous.get().toFile())
                  .withCache(new CHMCache())
                  .build());
    } else {
      this.anonymous = Optional.empty();
    }
  }

  @Override
  public IPDatabase read(
      boolean retainOriginalLine,
      LineReaderListener readerListener,
      LineProcessorListener processListener,
      DatabaseBuilderListener buildingListener,
      int workersCount) {
    final MaxmindNativeDatabase maxmindNativeDatabase =
        new MaxmindNativeDatabase(this.location, this.anonymous);
    return maxmindNativeDatabase;
  }

  @Override
  public ProviderKey provider() {
    return MaxmindProviderKey.of();
  }
}
