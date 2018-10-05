/*
 * Copyright (C) 2018 - present by Dice Technology Ltd.
 *
 * Please see distribution for license.
 */
package technology.dice.dicewhere.provider.dbip.reading;

import technology.dice.dicewhere.decorator.Decorator;
import technology.dice.dicewhere.decorator.DecoratorInformation;
import technology.dice.dicewhere.parsing.LineParser;
import technology.dice.dicewhere.provider.dbip.parsing.DbIpIpToLocationAndIspCSVLineParser;

import java.nio.file.Path;

public class DbIpLocarionAndIspLineReader extends DbIpLineReader {

  public DbIpLocarionAndIspLineReader(Path csv) {
    super(csv);
  }

  public DbIpLocarionAndIspLineReader(
      Path csv, Decorator<? extends DecoratorInformation> decorator) {
    super(csv, decorator);
  }

  @Override
  public LineParser buildLineParser(Decorator<? extends DecoratorInformation> decorator) {
    return new DbIpIpToLocationAndIspCSVLineParser(decorator);
  }
}
