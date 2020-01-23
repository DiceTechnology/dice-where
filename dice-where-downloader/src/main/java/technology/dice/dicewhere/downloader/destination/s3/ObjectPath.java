package technology.dice.dicewhere.downloader.destination.s3;

import technology.dice.dicewhere.downloader.commands.PathUtils;

public class ObjectPath {
  private final String bucket;
  private final String prefix;

  public static ObjectPath of(String prefix) {
    String sanitisedPrefix =
        PathUtils.removeLeadingCharacter(PathUtils.removeTrailingCharacter(prefix, "/"), "/");
    if (!sanitisedPrefix.contains("/")) {

      return new ObjectPath(sanitisedPrefix, "");
    }
    return new ObjectPath(
        prefix.substring(0, sanitisedPrefix.indexOf("/")),
        PathUtils.removeLeadingCharacter(
            sanitisedPrefix.substring(sanitisedPrefix.indexOf("/")), "/"));
  }

  private ObjectPath(String bucket, String prefix) {
    this.bucket = bucket;
    this.prefix = prefix;
  }

  public String getBucket() {
    return bucket;
  }

  public String getPrefix() {
    return prefix;
  }
}
