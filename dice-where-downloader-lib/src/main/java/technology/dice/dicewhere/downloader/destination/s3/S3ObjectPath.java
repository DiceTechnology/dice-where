package technology.dice.dicewhere.downloader.destination.s3;


import technology.dice.dicewhere.downloader.PathUtils;

public class S3ObjectPath {
  private final String bucket;
  private final String prefix;

  public static S3ObjectPath of(String prefix) {
    String sanitisedPrefix =
        PathUtils.removeLeadingCharacter(PathUtils.removeTrailingCharacter(prefix, "/"), "/");
    if (!sanitisedPrefix.contains("/")) {

      return new S3ObjectPath(sanitisedPrefix, "");
    }
    return new S3ObjectPath(
        prefix.substring(0, sanitisedPrefix.indexOf("/")),
        PathUtils.removeLeadingCharacter(
            sanitisedPrefix.substring(sanitisedPrefix.indexOf("/")), "/"));
  }

  private S3ObjectPath(String bucket, String prefix) {
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
