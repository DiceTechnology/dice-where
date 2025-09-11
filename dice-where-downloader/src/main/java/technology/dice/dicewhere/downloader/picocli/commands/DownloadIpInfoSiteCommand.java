package technology.dice.dicewhere.downloader.picocli.commands;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import technology.dice.dicewhere.downloader.actions.DownloadExecutionResult;
import technology.dice.dicewhere.downloader.actions.ipinfo.DownloadIpInfoSite;

import java.util.Optional;

@Command(
    name = "ipinfo-site",
    description = "Downloads the selected IpInfo dataset from IpInfo's website")
public class DownloadIpInfoSiteCommand extends IpInfoBaseCommand {

  @Option(
      names = {"-t", "--token"},
      required = false,
      description = "The ipinfo download key")
  String token;

    @Option(names = "--token:env",
            required = false,
            description = "The ipinfo download key env variable")
    private String tokenEnvVariable;

  @Parameters(
      index = "0",
      description =
          "The destination of the file. Must start with the scheme (s3:// or file://). S3 destinations require credentials to be provided through the default chain for AWS Java SDK. See https://docs.aws.amazon.com/sdk-for-java/v2/developer-guide/credentials.html for more details")
  String destination;

  @Override
  public DownloadExecutionResult execute() {
      var secretToken = Optional.ofNullable(token)
              .or(() -> Optional.ofNullable(System.getenv(tokenEnvVariable)))
              .orElseThrow(() -> new IllegalStateException("Token param or api key env var should be provided"));

    return new DownloadIpInfoSite(
            noCheckMd5, overwrite, verbose, dataset, format, secretToken, destination)
            .execute();
  }
}
