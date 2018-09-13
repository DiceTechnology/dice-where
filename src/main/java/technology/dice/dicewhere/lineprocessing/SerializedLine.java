package technology.dice.dicewhere.lineprocessing;

import technology.dice.dicewhere.api.api.IP;
import technology.dice.dicewhere.parsing.ParsedLine;

public class SerializedLine {
  private ParsedLine parsedLine;
  private final IP startIp;
  private final byte[] info;

  public SerializedLine(IP startIp, byte[] info, ParsedLine parsedLine) {
    this.startIp = startIp;
    this.info = info;
    this.parsedLine = parsedLine;
  }

  public IP getStartIp() {
    return startIp;
  }

  public byte[] getInfo() {
    return info;
  }

  public ParsedLine getParsedLine() {
    return parsedLine;
  }
}
