package technology.dice.dicewhere.downloader;

public class PathUtils {
  public static String removeLeadingCharacter(String original, String charToRemove) {
    return original.startsWith(charToRemove) ? original.substring(1) : original;
  }

  public static String removeTrailingCharacter(String original, String charToRemove) {
    return original.endsWith(charToRemove)
        ? original.substring(0, original.length() - 1)
        : original;
  }
}
