package technology.dice.dicewhere.parsing;

import technology.dice.dicewhere.api.exceptions.LineParsingException;
import technology.dice.dicewhere.reading.RawLine;

import java.util.stream.Stream;

public interface LineParser {
  Stream<ParsedLine> parse(RawLine rawLine, boolean retainOriginalLine) throws LineParsingException;

  default Stream<ParsedLine> parse(RawLine rawLine) throws LineParsingException {
    return parse(rawLine, false);
  }
}
