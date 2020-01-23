package technology.dice.dicewhere.downloader.commands.maxmind;

import java.net.URI;
import picocli.CommandLine.Option;
import technology.dice.dicewhere.downloader.commands.PathUtils;
import technology.dice.dicewhere.downloader.picocli.BaseCommand;

public abstract class MaxmindBaseCommand extends BaseCommand {

  @Option(
      names = {"-e", "--edition"},
      defaultValue = "GeoIP2",
      description = "The database edition to download. Valid values: ${COMPLETION-CANDIDATES}")
  MaxmindEdition edition;

  @Option(
      names = {"-d", "--database"},
      required = true,
      description = "The database to download. Valid values: ${COMPLETION-CANDIDATES}")
  MaxmindDatabase database;

  @Option(
      names = {"-f", "--format"},
      defaultValue = "CSV",
      description = "The database format to download. Valid values: ${COMPLETION-CANDIDATES}")
  MaxmindFormat format;

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
