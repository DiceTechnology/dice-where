/*
 * Copyright (C) 2018 - present by Dice Technology Ltd.
 *
 * Please see distribution for license.
 */
package technology.dice.dicewhere.provider.dbip.reading;

import technology.dice.dicewhere.building.DatabaseBuilder;
import technology.dice.dicewhere.decorator.Decorator;
import technology.dice.dicewhere.decorator.DecoratorInformation;
import technology.dice.dicewhere.parsing.LineParser;
import technology.dice.dicewhere.provider.dbip.parsing.DbIpIpToCityLiteCSVLineParser;

import java.nio.file.Path;

public class DbIpCityLiteLineReader extends DbIpLineReader {

  public DbIpCityLiteLineReader(Path csv) {
    this(csv, null);
  }

  public DbIpCityLiteLineReader(Path csv, Decorator<? extends DecoratorInformation> decorator) {
    this(csv, decorator, DatabaseBuilder.StorageMode.FILE);
  }

  public DbIpCityLiteLineReader(
      Path csv,
      Decorator<? extends DecoratorInformation> decorator,
      DatabaseBuilder.StorageMode storageMode) {
    super(csv, decorator, storageMode);
  }

  @Override
  public LineParser buildLineParser(Decorator<? extends DecoratorInformation> decorator) {
    return new DbIpIpToCityLiteCSVLineParser(decorator);
  }
}
