/*
 * Copyright (C) 2018 - present by Dice Technology Ltd.
 *
 * Please see distribution for license.
 */
package technology.dice.dicewhere.provider.dbip.reading;

import technology.dice.dicewhere.decorator.Decorator;
import technology.dice.dicewhere.decorator.DecoratorInformation;
import technology.dice.dicewhere.parsing.LineParser;
import technology.dice.dicewhere.provider.dbip.parsing.DbIpIpToCountryLiteCSVLineParser;

import java.nio.file.Path;

public class DbIpCountryLiteLineReader extends DbIpLineReader {

  public DbIpCountryLiteLineReader(Path csv) {
    super(csv);
  }

  public DbIpCountryLiteLineReader(
      Path csv, Decorator<? extends DecoratorInformation> decorator) {
    super(csv, decorator);
  }

  @Override
  public LineParser buildLineParser(Decorator<? extends DecoratorInformation> decorator) {
    return new DbIpIpToCountryLiteCSVLineParser(decorator);
  }
}
