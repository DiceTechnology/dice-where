package technology.dice.dicewhere.downloader.commands.maxmind;

import java.io.IOException;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import technology.dice.dicewhere.downloader.ObjectMapperInstance;
import technology.dice.dicewhere.downloader.commands.CommandExecutionResult;
import technology.dice.dicewhere.downloader.commands.PathUtils;
import technology.dice.dicewhere.downloader.destination.FileAcceptor;
import technology.dice.dicewhere.downloader.destination.FileAcceptorFactory;
import technology.dice.dicewhere.downloader.destination.s3.Latest;
import technology.dice.dicewhere.downloader.destination.s3.ObjectPath;
import technology.dice.dicewhere.downloader.exception.DownloaderException;
import technology.dice.dicewhere.downloader.source.s3.S3Source;

@Command(
    name = "maxmind-s3",
    description =
        "Downloads the selected Maxmind edition of a database from S3. This command requires credentials to be provided through the default chain for AWS Java SDK. See https://docs.aws.amazon.com/sdk-for-java/v2/developer-guide/credentials.html for more details")
public class DownloadMaxmindS3 extends MaxmindBaseCommand {
  private static final Logger LOG = LoggerFactory.getLogger(DownloadMaxmindS3.class);

  @Parameters(index = "0")
  String prefix;

  @Parameters(
      index = "1",
      description =
          "The destination of the file. Must start with the scheme (s3:// or file://). S3 destinations require credentials to be provided through the default chain for AWS Java SDK. See https://docs.aws.amazon.com/sdk-for-java/v2/developer-guide/credentials.html for more details")
  String destination;

  /**
   * Default constructor to be used by picocli when running from the command line
   */
  public DownloadMaxmindS3() {
    super();
  }

  /**
   * Constructor to be used when running programmatically
   */
  public DownloadMaxmindS3(
      boolean noCheckMd5,
      boolean overwrite,
      boolean verbose,
      MaxmindEdition edition,
      MaxmindDatabase database,
      MaxmindFormat format,
      String prefix,
      String destination) {
    super(noCheckMd5, overwrite, verbose, edition, database, format);
    this.prefix = prefix;
    this.destination = destination;
  }

  @Override
  public CommandExecutionResult execute() {
    final ObjectPath objectPath = ObjectPath.of(prefix);
    S3Client s3Client = S3Client.create();
    Optional<String> optionalKey = latestKeyForDatabase(s3Client, objectPath);
    if (!optionalKey.isPresent()) {
      LOG.error("Could not find the latest version of the database at the source");
      return new CommandExecutionResult(false, false);
    }

    String key = optionalKey.get();

    S3Source s3Source = new S3Source(s3Client, objectPath.getBucket(), key);

    FileAcceptor<?, ?> acceptor =
        FileAcceptorFactory.acceptorFor(
            this.uriForTarget(this.destination, s3Source.fileInfo().getFileName()));

    return this.process(acceptor, s3Source);
  }

  private Optional<String> latestKeyForDatabase(S3Client s3Client, ObjectPath objectPath) {
    GetObjectRequest getObjectRequest =
        GetObjectRequest.builder()
            .bucket(objectPath.getBucket())
            .key(
                PathUtils.removeLeadingCharacter(
                    objectPath.getPrefix()
                        + "/maxmind/"
                        + edition.name()
                        + "/"
                        + database.name()
                        + "/"
                        + format.name()
                        + "/latest",
                    "/"))
            .build();
    try (final ResponseInputStream<GetObjectResponse> object =
        s3Client.getObject(getObjectRequest); ) {
      final Latest latest = ObjectMapperInstance.INSTANCE.readValue(object, Latest.class);
      return Optional.ofNullable(latest.getKey());
    } catch (IOException | NoSuchKeyException e) {
      throw new DownloaderException(
          "Latest version information not readable for "
              + edition
              + " - "
              + database.name()
              + " in "
              + format.name()
              + " format.",
          e);
    }
  }
}
