package technology.dice.dicewhere.downloader.commands;

import java.util.concurrent.Callable;
import picocli.CommandLine.Command;
import picocli.CommandLine.HelpCommand;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.ParameterException;
import picocli.CommandLine.Spec;
import technology.dice.dicewhere.downloader.commands.maxmind.DownloadMaxmindS3;
import technology.dice.dicewhere.downloader.commands.maxmind.DownloadMaxmindSite;
import technology.dice.dicewhere.downloader.picocli.ResourceVersionProvider;

@Command(
    name = "dice-where-downloader",
    mixinStandardHelpOptions = true,
    showDefaultValues = true,
    versionProvider = ResourceVersionProvider.class,
    subcommands = {HelpCommand.class, DownloadMaxmindSite.class, DownloadMaxmindS3.class},
    synopsisSubcommandLabel = "COMMAND")
public class DownloadCommand implements Callable<Integer> {
  @Spec CommandSpec spec;

  @Override
  public Integer call() throws Exception {
    throw new ParameterException(spec.commandLine(), "Missing required command");
  }
}
