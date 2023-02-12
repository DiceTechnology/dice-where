package technology.dice.dicewhere.downloader.picocli.commands;

import java.util.concurrent.Callable;
import picocli.CommandLine.Command;
import picocli.CommandLine.HelpCommand;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.ParameterException;
import picocli.CommandLine.Spec;
import technology.dice.dicewhere.downloader.picocli.ResourceVersionProvider;

@Command(
    name = "dice-where-downloader",
    mixinStandardHelpOptions = true,
    showDefaultValues = true,
    versionProvider = ResourceVersionProvider.class,
    subcommands = {
      HelpCommand.class,
      DownloadMaxmindSiteCommand.class,
      DownloadMaxmindS3Command.class,
      DownloadIpInfoSiteCommand.class
    },
    synopsisSubcommandLabel = "COMMAND")
public class DownloadCommand implements Callable<Integer> {
  @Spec CommandSpec spec;

  @Override
  public Integer call() {
    throw new ParameterException(spec.commandLine(), "Missing required command");
  }
}
