/*
 * Copyright (C) 2018 - present by Dice Technology Ltd.
 *
 * Please see distribution for license.
 */

package technology.dice.dicewhere.parsing;

import technology.dice.dicewhere.api.exceptions.LineParsingException;
import technology.dice.dicewhere.reading.RawLine;

public interface LineParser {
  ParsedLine parse(RawLine rawLine, boolean retainOriginalLine) throws LineParsingException;

  default ParsedLine parse(RawLine rawLine) throws LineParsingException {
    return parse(rawLine, false);
  }
}
