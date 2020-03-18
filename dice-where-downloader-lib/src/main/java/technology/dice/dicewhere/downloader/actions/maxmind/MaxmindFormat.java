package technology.dice.dicewhere.downloader.actions.maxmind;

public enum MaxmindFormat {
  BINARY("", "tar.gz"),
  CSV("-CSV", "zip");

  private final String suffix;
  private final String remoteName;

  MaxmindFormat(String remoteName, String suffix) {
    this.remoteName = remoteName;
    this.suffix = suffix;
  }

  public String getSuffix() {
    return suffix;
  }

  public String getRemoteName() {
    return remoteName;
  }
}
