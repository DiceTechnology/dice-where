package technology.dice.dicewhere.parsing;


import technology.dice.dicewhere.api.exceptions.LineParsingException;
import technology.dice.dicewhere.reading.RawLine;

import java.util.Objects;
import java.util.regex.Pattern;

public interface LineParser {
	Pattern quotesRemover = Pattern.compile("\"", Pattern.LITERAL);

	ParsedLine parse(RawLine rawLine, boolean retainOriginalLine) throws LineParsingException;

	default ParsedLine parse(RawLine rawLine) throws LineParsingException {
		return this.parse(rawLine, false);
	}

	default String removeQuotes(String countryCode) {
		return quotesRemover.matcher(Objects.requireNonNull(countryCode)).replaceAll("");
	}
}
