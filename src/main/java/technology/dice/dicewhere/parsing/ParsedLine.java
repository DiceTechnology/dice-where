package technology.dice.dicewhere.parsing;

import technology.dice.dicewhere.api.api.IP;
import technology.dice.dicewhere.api.api.IpInformation;
import technology.dice.dicewhere.reading.RawLine;

import java.util.Objects;

public class ParsedLine {
  private final RawLine rawLine;
  private final IP startIp;
  private final IP endIp;
  private final IpInformation info;

  public ParsedLine(IP startIp, IP endIp, IpInformation info, RawLine rawLine) {
    this.startIp = startIp;
    this.endIp = endIp;
    this.info = info;
    this.rawLine = rawLine;
  }

  public IP getStartIp() {
    return startIp;
  }

  public IP getEndIp() {
    return endIp;
  }

  public IpInformation getInfo() {
    return info;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof ParsedLine)) {
      return false;
    }
    ParsedLine that = (ParsedLine) o;
    return Objects.equals(rawLine, that.rawLine)
        && Objects.equals(startIp, that.startIp)
        && Objects.equals(endIp, that.endIp)
        && Objects.equals(info, that.info);
  }

  @Override
  public int hashCode() {

    return Objects.hash(rawLine, startIp, endIp, info);
  }

	@Override
	public String toString() {
		return "ParsedLine{" +
				"rawLine=" + rawLine +
				", startIp=" + startIp +
				", endIp=" + endIp +
				", info=" + info +
				'}';
	}
}
