package technology.dice.dicewhere.utils;

import java.util.Optional;
import java.util.regex.Pattern;

public abstract class StringUtils {
  private static final Pattern quotesRemover = Pattern.compile("\"", Pattern.LITERAL);

  /*
  This method outperforms String.replaceAll() by avoiding compiling a regex on each execution
  */
  public static String removeQuotes(String string) {
    return isNullOrEmpty(string) ? string : quotesRemover.matcher(string).replaceAll("");
  }

  public static boolean isNullOrEmpty(String string) {
    return string == null || "".equals(string);
  }

  public static Optional<String> nonEmptyString(String string) {
    return StringUtils.isNullOrEmpty(string) ? Optional.empty() : Optional.of(string);
  }
}
