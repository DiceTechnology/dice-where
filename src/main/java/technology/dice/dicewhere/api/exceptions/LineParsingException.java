/*
 * Copyright (C) 2018 - present by Dice Technology Ltd.
 *
 * Please see distribution for license.
 */

package technology.dice.dicewhere.api.exceptions;

import technology.dice.dicewhere.reading.RawLine;

public class LineParsingException extends Exception {
  private final RawLine offendingLine;

  public LineParsingException(Throwable cause, RawLine offendingLine) {
    super(cause);
    this.offendingLine = offendingLine;
  }

  public LineParsingException(String message, RawLine offendingLine) {
    super(message);
    this.offendingLine = offendingLine;
  }

  public RawLine getOffendingLine() {
    return offendingLine;
  }
}
