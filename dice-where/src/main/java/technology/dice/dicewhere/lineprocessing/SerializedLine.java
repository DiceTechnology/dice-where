/*
 * Copyright (C) 2018 - present by Dice Technology Ltd.
 *
 * Please see distribution for license.
 */

package technology.dice.dicewhere.lineprocessing;

import technology.dice.dicewhere.api.api.IP;
import technology.dice.dicewhere.parsing.ParsedLine;

public class SerializedLine {
  private ParsedLine parsedLine;
  private final IP startIp;

  public SerializedLine(IP startIp, ParsedLine parsedLine) {
    this.startIp = startIp;
    this.parsedLine = parsedLine;
  }

  public IP getStartIp() {
    return startIp;
  }

  public ParsedLine getParsedLine() {
    return parsedLine;
  }
}
