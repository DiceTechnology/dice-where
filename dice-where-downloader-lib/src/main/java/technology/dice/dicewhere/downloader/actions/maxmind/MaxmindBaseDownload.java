package technology.dice.dicewhere.downloader.actions.maxmind;

import java.net.URI;
import technology.dice.dicewhere.downloader.Download;
import technology.dice.dicewhere.downloader.PathUtils;

public abstract class MaxmindBaseDownload extends Download {

  protected final MaxmindEdition edition;

  protected final MaxmindDatabase database;

  protected final MaxmindFormat format;

  public MaxmindBaseDownload(
      boolean noCheckMd5,
      boolean overwrite,
      boolean verbose,
      MaxmindEdition edition,
      MaxmindDatabase database,
      MaxmindFormat format) {
    super(noCheckMd5, overwrite, verbose);
    this.edition = edition;
    this.database = database;
    this.format = format;
  }

  protected URI uriForTarget(String destinationBase, String fileName) {
    return URI.create(
        (PathUtils.removeTrailingCharacter(destinationBase, "/") + "/maxmind/")
            + edition.name()
            + "/"
            + database.name()
            + "/"
            + format.name()
            + "/"
            + fileName);
  }
}
