/*
 * Copyright (C) 2018 - present by Dice Technology Ltd.
 *
 * Please see distribution for license.
 */

package technology.dice.dicewhere.provider.dbip.reading;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import technology.dice.dicewhere.building.navigablemap.MapDbDatabaseBuilder;
import technology.dice.dicewhere.decorator.Decorator;
import technology.dice.dicewhere.decorator.DecoratorInformation;
import technology.dice.dicewhere.parsing.LineParser;
import technology.dice.dicewhere.provider.ProviderKey;
import technology.dice.dicewhere.provider.dbip.DbIpProviderKey;
import technology.dice.dicewhere.reading.CSVLineReader;

import java.io.IOException;
import java.nio.file.Path;
import java.util.stream.Stream;

public abstract class DbIpLineReader extends CSVLineReader {
  private static final int BUFFER_SIZE = 1024 * 1024;
  private final LineParser lineParser;
  private final Path csv;

  public DbIpLineReader(@NotNull Path csv) {
    this(csv, null);
  }

  public DbIpLineReader(
      @NotNull Path csv, @Nullable Decorator<? extends DecoratorInformation> decorator) {
    this(csv, decorator, MapDbDatabaseBuilder.StorageMode.FILE);
  }

  public DbIpLineReader(
      @NotNull Path csv,
      @Nullable Decorator<? extends DecoratorInformation> decorator,
      @NotNull MapDbDatabaseBuilder.StorageMode storageMode) {
    super(storageMode);
    lineParser = buildLineParser(decorator);
    this.csv = csv;
  }

  public abstract LineParser buildLineParser(Decorator<? extends DecoratorInformation> decorator);

  @Override
  protected Stream<String> lines() throws IOException {
    return bufferedReaderForPath(csv, BUFFER_SIZE).lines();
  }

  @Override
  public ProviderKey provider() {
    return DbIpProviderKey.of();
  }

  @Override
  public LineParser parser() {
    return lineParser;
  }
}
