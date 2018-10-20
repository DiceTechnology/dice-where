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

  protected abstract Optional<Decorator<? extends DecoratorInformation>> getDecorator();

  public Stream<ParsedLine> parse(RawLine rawLine, boolean retainOriginalLine)
      throws LineParsingException {
    IpInformation parsedInfo = this.parseLine(rawLine, retainOriginalLine);
    try {
      return decorateParsedLine(parsedInfo, rawLine);
    } catch (Exception e) {
      // This is very general but because it is running in a separate thread,
      //many exception will just get swallowed making debugging difficult.
      throw new LineParsingException(e, rawLine);
    }
  }

  protected abstract IpInformation parseLine(RawLine rawLine, boolean retainOriginalLine)
      throws LineParsingException;

  private Stream<ParsedLine> decorateParsedLine(IpInformation ipInfo, RawLine rawLine)
      throws UnknownHostException {
    if (getDecorator().isPresent()) {
      Stream<IpInformation> decoratedIpInfo = getDecorator().get().decorate(ipInfo);
      return decoratedIpInfo.map(
          info -> new ParsedLine(info.getStartOfRange(), info.getEndOfRange(), info, rawLine));
    } else {
      return Stream.of(
          new ParsedLine(ipInfo.getStartOfRange(), ipInfo.getEndOfRange(), ipInfo, rawLine));
    }
  }
}
