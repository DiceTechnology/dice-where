package technology.dice.dicewhere.utils;

import java.util.regex.Pattern;

public abstract class StringUtils {
	private static final Pattern quotesRemover = Pattern.compile("\"", Pattern.LITERAL);

	/*
		This method outperforms String.replaceAll() by avoiding compiling a regex on each execution
	 */
	public static String removeQuotes(String string) {
		return quotesRemover.matcher(string).replaceAll("");
	}
}
