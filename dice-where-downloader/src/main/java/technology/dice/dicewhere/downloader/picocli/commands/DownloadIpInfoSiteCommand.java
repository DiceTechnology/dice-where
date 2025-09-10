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

  @Option(names = {"-ak", "--api-key"},
          required = false,
          defaultValue = "${env:IPINFO_API_KEY}",
          description = "The ipinfo download key (env var)")
  private String apiKey;

  @Parameters(
      index = "0",
      description =
          "The destination of the file. Must start with the scheme (s3:// or file://). S3 destinations require credentials to be provided through the default chain for AWS Java SDK. See https://docs.aws.amazon.com/sdk-for-java/v2/developer-guide/credentials.html for more details")
  String destination;

  @Override
  public DownloadExecutionResult execute() {
    String secretToken = Optional.of(apiKey)
            .or(() -> Optional.of(token))
            .orElseThrow(() -> new IllegalStateException("Token or api key parameters should be provided"));

    return new DownloadIpInfoSite(
            noCheckMd5, overwrite, verbose, dataset, format, secretToken, destination)
            .execute();
  }
}
