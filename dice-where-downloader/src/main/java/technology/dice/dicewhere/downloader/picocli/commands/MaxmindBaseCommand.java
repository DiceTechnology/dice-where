package technology.dice.dicewhere.downloader.picocli.commands;

import picocli.CommandLine.Option;
import technology.dice.dicewhere.downloader.actions.maxmind.MaxmindDatabase;
import technology.dice.dicewhere.downloader.actions.maxmind.MaxmindEdition;
import technology.dice.dicewhere.downloader.actions.maxmind.MaxmindFormat;

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
}
