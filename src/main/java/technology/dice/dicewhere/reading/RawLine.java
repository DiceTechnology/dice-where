/*
 * Copyright (C) 2018 - present by Dice Technology Ltd.
 *
 * Please see distribution for license.
 */

package technology.dice.dicewhere.reading;

import java.util.Objects;

public class RawLine {
  private final String line;
  private final long lineNumber;

  public RawLine(String line, long lineNumber) {
    this.line = line;
    this.lineNumber = lineNumber;
  }

  public String getLine() {
    return line;
  }

  public long getLineNumber() {
    return lineNumber;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof RawLine)) {
      return false;
    }
    RawLine rawLine = (RawLine) o;
    return lineNumber == rawLine.lineNumber && Objects.equals(line, rawLine.line);
  }

  @Override
  public int hashCode() {
    return Objects.hash(line, lineNumber);
  }
}
