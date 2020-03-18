/*
 * Copyright (C) 2018 - present by Dice Technology Ltd.
 *
 * Please see distribution for license.
 */
package technology.dice.dicewhere.provider.dbip.reading;

import technology.dice.dicewhere.building.navigablemap.MapDbDatabaseBuilder;
import technology.dice.dicewhere.decorator.Decorator;
import technology.dice.dicewhere.decorator.DecoratorInformation;
import technology.dice.dicewhere.parsing.LineParser;
import technology.dice.dicewhere.provider.dbip.parsing.DbIpIpToLocationAndIspCSVLineParser;

import java.nio.file.Path;

public class DbIpLocationAndIspLineReader extends DbIpLineReader {

  public DbIpLocationAndIspLineReader(Path csv) {
    this(csv, null);
  }

  public DbIpLocationAndIspLineReader(
      Path csv, Decorator<? extends DecoratorInformation> decorator) {
    this(csv, decorator, MapDbDatabaseBuilder.StorageMode.FILE);
  }

  public DbIpLocationAndIspLineReader(
      Path csv,
      Decorator<? extends DecoratorInformation> decorator,
      MapDbDatabaseBuilder.StorageMode storageMode) {
    super(csv, decorator, storageMode);
  }

  @Override
  public LineParser buildLineParser(Decorator<? extends DecoratorInformation> decorator) {
    return new DbIpIpToLocationAndIspCSVLineParser(decorator);
  }
}
