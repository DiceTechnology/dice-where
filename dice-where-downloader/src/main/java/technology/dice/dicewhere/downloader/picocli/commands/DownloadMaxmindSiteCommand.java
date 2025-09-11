package technology.dice.dicewhere.downloader.picocli.commands;

import org.slf4j.LoggerFactory;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import technology.dice.dicewhere.downloader.actions.DownloadExecutionResult;
import technology.dice.dicewhere.downloader.actions.maxmind.DownloadMaxmindSite;

import java.util.Optional;

@Command(
    name = "maxmind-site",
    description = "Downloads the selected Maxmind edition of a database from Maxmind's website")
public class DownloadMaxmindSiteCommand extends MaxmindBaseCommand {

    static {
        LoggerFactory.getLogger(DownloadMaxmindSiteCommand.class);
    }

    @Option(
        names = {"-k", "--key"},
        required = false,
        defaultValue = "${env:MAXMIND_API_KEY}",
        description = "The maxmind download key")
  String key;

  @Parameters(
      index = "0",
      description =
          "The destination of the file. Must start with the scheme (s3:// or file://). S3 destinations require credentials to be provided through the default chain for AWS Java SDK. See https://docs.aws.amazon.com/sdk-for-java/v2/developer-guide/credentials.html for more details")
  String destination;

  @Override
  public DownloadExecutionResult execute() {
    return new DownloadMaxmindSite(
            noCheckMd5, overwrite, verbose, edition, database, format, key, destination)
        .execute();
  }
}
