package technology.dice.dicewhere.downloader.picocli.commandssssss;

import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import technology.dice.dicewhere.downloader.actions.DownloadExecutionResult;
import technology.dice.dicewhere.downloader.actions.maxmind.DownloadMaxmindS3;

@Command(
    name = "maxmind-s3",
    description =
        "Downloads the selected Maxmind edition of a database from S3. This command requires credentials to be provided through the default chain for AWS Java SDK. See https://docs.aws.amazon.com/sdk-for-java/v2/developer-guide/credentials.html for more details")
public class DownloadMaxmindS3Command extends MaxmindBaseCommand {
  private static final Logger LOG = LoggerFactory.getLogger(DownloadMaxmindS3Command.class);
  private Optional<AwsCredentialsProvider> awsCredentials = Optional.empty();
  private Optional<Region> awsRegion = Optional.empty();

  @Parameters(index = "0")
  String prefix;

  @Parameters(
      index = "1",
      description =
          "The destination of the file. Must start with the scheme (s3:// or file://). S3 destinations require credentials to be provided through the default chain for AWS Java SDK. See https://docs.aws.amazon.com/sdk-for-java/v2/developer-guide/credentials.html for more details")
  String destination;

  @Override
  public DownloadExecutionResult execute() {
    return new DownloadMaxmindS3(
            noCheckMd5, overwrite, verbose, edition, database, format, prefix, destination)
        .execute();
  }
}
