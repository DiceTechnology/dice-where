package technology.dice.dicewhere.downloader.md5;

public class MD5Checksum {
  private String md5;

  public static MD5Checksum of(String md5) {
    return new MD5Checksum(md5);
  }

  private MD5Checksum(String md5) {
    this.md5 = md5;
  }

  public boolean matches(String md5) {
    return this.matches(MD5Checksum.of(md5));
  }

  public boolean matches(MD5Checksum md5) {
    if (md5 == null) {
      return false;
    }
    return this.md5.equalsIgnoreCase(md5.md5);
  }

  public String stringFormat() {
    return this.md5.toLowerCase();
  }
}
