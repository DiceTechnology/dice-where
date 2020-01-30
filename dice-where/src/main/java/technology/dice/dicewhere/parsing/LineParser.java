/*
 * Copyright (C) 2018 - present by Dice Technology Ltd.
 *
 * Please see distribution for license.
 */

package technology.dice.dicewhere.parsing;

import technology.dice.dicewhere.api.api.IpInformation;
import technology.dice.dicewhere.api.exceptions.LineParsingException;
import technology.dice.dicewhere.decorator.Decorator;
import technology.dice.dicewhere.decorator.DecoratorInformation;
import technology.dice.dicewhere.reading.RawLine;

import java.net.UnknownHostException;
import java.util.Optional;
import java.util.stream.Stream;

public abstract class LineParser {

  public abstract Optional<Decorator<? extends DecoratorInformation>> getDecorator();

  public Stream<ParsedLine> parse(RawLine rawLine, boolean retainOriginalLine)
      throws LineParsingException {
    IpInformation parsedInfo = this.parseLine(rawLine, retainOriginalLine);
    return Stream.of(
        new ParsedLine(
            parsedInfo.getStartOfRange(), parsedInfo.getEndOfRange(), parsedInfo, rawLine));
  }

  protected abstract IpInformation parseLine(RawLine rawLine, boolean retainOriginalLine)
      throws LineParsingException;
}
