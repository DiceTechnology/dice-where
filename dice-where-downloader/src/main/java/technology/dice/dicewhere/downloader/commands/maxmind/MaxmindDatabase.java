package technology.dice.dicewhere.downloader.commands.maxmind;

public enum MaxmindDatabase {
  City("City"),
  Anonymous("Anonymous-IP");

  private final String remoteName;

  MaxmindDatabase(String name) {
    this.remoteName = name;
  }

  public String getRemoteName() {
    return remoteName;
  }
}
