package technology.dice.dicewhere.downloader.actions.ipinfo;

public enum IpInfoFormat {
  BINARY("mmdb");

  private final String suffix;

  IpInfoFormat(String suffix) {
    this.suffix = suffix;
  }

  public String getSuffix() {
    return suffix;
  }
}
