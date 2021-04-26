package technology.dice.dicewhere.downloader.main;

import picocli.CommandLine;
import technology.dice.dicewhere.downloader.picocli.commandssssss.DownloadCommand;
import technology.dice.dicewhere.downloader.picocli.PrintExceptionMessageHandler;

/**
 * Utility program to assist with the download of IP to Geolocation databases.
 */
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
