/*
 * Copyright (C) 2018 - present by Dice Technology Ltd.
 *
 * Please see distribution for license.
 */

package technology.dice.dicewhere.provider.dbip.reading;

import java.io.IOException;
import java.nio.file.Path;
import java.util.stream.Stream;

import technology.dice.dicewhere.decorator.Decorator;
import technology.dice.dicewhere.decorator.DecoratorInformation;
import technology.dice.dicewhere.parsing.LineParser;
import technology.dice.dicewhere.provider.ProviderKey;
import technology.dice.dicewhere.provider.dbip.DbIpProviderKey;
import technology.dice.dicewhere.provider.dbip.parsing.DbIpIpToLocationAndIspCSVLineParser;
import technology.dice.dicewhere.reading.LineReader;

public class DbIpLineReader extends LineReader {
  private static final int BUFFER_SIZE = 1024 * 1024;
  private final LineParser lineParser;
  private final Path csv;

  //TODO: this needs to somehow switch between different readers, depending on the DB that is being used.
  //although we have the readers, we don't have the mechanics to switch between.

  public DbIpLineReader(Path csv) {
    this(csv, null);
  }

  public DbIpLineReader(Path csv, Decorator<? extends DecoratorInformation> decorator) {
    lineParser = new DbIpIpToLocationAndIspCSVLineParser(decorator);
    this.csv = csv;
  }

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
