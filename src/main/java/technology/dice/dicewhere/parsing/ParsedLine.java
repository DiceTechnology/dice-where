package technology.dice.dicewhere.parsing;


import technology.dice.dicewhere.api.api.IP;
import technology.dice.dicewhere.api.api.IPInformation;
import technology.dice.dicewhere.reading.RawLine;

import java.util.Objects;

/**
 * Created by gluiz on 05/07/2018.
 */
public class ParsedLine {
	private final RawLine rawLine;
	private final IP startIp;
	private final IP endIp;
	private final IPInformation info;

	public ParsedLine(IP startIp, IP endIp, IPInformation info, RawLine rawLine) {
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

	public IPInformation getInfo() {
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
		return Objects.equals(rawLine, that.rawLine) &&
				Objects.equals(startIp, that.startIp) &&
				Objects.equals(endIp, that.endIp) &&
				Objects.equals(info, that.info);
	}

	@Override
	public int hashCode() {

		return Objects.hash(rawLine, startIp, endIp, info);
	}

}