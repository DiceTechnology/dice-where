package technology.dice.dicewhere.parsing;


import technology.dice.dicewhere.api.exceptions.LineParsingException;
import technology.dice.dicewhere.reading.RawLine;
import technology.dice.dicewhere.utils.StringUtils;

import java.util.Objects;

public interface LineParser {
	ParsedLine parse(RawLine rawLine, boolean retainOriginalLine) throws LineParsingException;

	default ParsedLine parse(RawLine rawLine) throws LineParsingException {
		return this.parse(rawLine, false);
	}

	default String removeQuotes(String countryCode) {
		return StringUtils.removeQuotes(Objects.requireNonNull(countryCode));
	}
}
