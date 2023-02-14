package technology.dice.dicewhere.downloader.picocli.commands;

import picocli.CommandLine.Option;
import technology.dice.dicewhere.downloader.actions.ipinfo.IpInfoDataset;
import technology.dice.dicewhere.downloader.actions.ipinfo.IpInfoFormat;

public abstract class IpInfoBaseCommand extends BaseCommand {

  @Option(
      names = {"-d", "--dataset"},
      required = true,
      description = "The dataset to download. Valid values: ${COMPLETION-CANDIDATES}")
  IpInfoDataset dataset;

  @Option(
      names = {"-f", "--format"},
      defaultValue = "BINARY",
      description = "The database format to download. Valid values: ${COMPLETION-CANDIDATES}")
  IpInfoFormat format;
}
