package technology.dice.dicewhere.downloader.main;

import picocli.CommandLine;
import technology.dice.dicewhere.downloader.commands.DownloadCommand;
import technology.dice.dicewhere.downloader.picocli.PrintExceptionMessageHandler;

public class Main {
  public static void main(String[] args) {
    final int result =
        new CommandLine(new DownloadCommand())
            .setCaseInsensitiveEnumValuesAllowed(true)
            .setExecutionExceptionHandler(new PrintExceptionMessageHandler())
            .execute(args);
    System.exit(result);
  }
}
