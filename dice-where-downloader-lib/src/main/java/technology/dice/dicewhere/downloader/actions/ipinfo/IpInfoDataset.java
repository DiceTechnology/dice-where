package technology.dice.dicewhere.downloader.actions.ipinfo;

public enum IpInfoDataset {
  STANDARD_LOCATION("standard_location"),
  PRIVACY("privacy");

  private final String remoteName;

  IpInfoDataset(String name) {
    this.remoteName = name;
  }

  public String getRemoteName() {
    return remoteName;
  }
}
