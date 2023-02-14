package technology.dice.dicewhere.downloader.actions.maxmind;

import java.io.IOException;
import java.net.URI;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.utils.Pair;
import technology.dice.dicewhere.downloader.ObjectMapperInstance;
import technology.dice.dicewhere.downloader.actions.DownloadExecutionResult;
import technology.dice.dicewhere.downloader.actions.S3ClientConfig;
import technology.dice.dicewhere.downloader.destination.FileAcceptor;
import technology.dice.dicewhere.downloader.destination.FileAcceptorFactory;
import technology.dice.dicewhere.downloader.destination.s3.Latest;
import technology.dice.dicewhere.downloader.destination.s3.S3DownloadSetup;
import technology.dice.dicewhere.downloader.destination.s3.S3ObjectPath;
import technology.dice.dicewhere.downloader.exception.DownloaderException;
import technology.dice.dicewhere.downloader.source.s3.S3Source;

public class DownloadMaxmindS3 extends MaxmindBaseDownload {
  private static final Logger LOG = LoggerFactory.getLogger(DownloadMaxmindS3.class);
  private final Optional<S3ClientConfig> s3ClientConfig;

  private final String prefix;
  private final String destination;

  private DownloadMaxmindS3(
      Optional<S3ClientConfig> s3ClientConfig,
      boolean noCheckMd5,
      boolean overwrite,
      boolean verbose,
      MaxmindEdition edition,
      MaxmindDatabase database,
      MaxmindFormat format,
      String s3BucketPrefix,
      String destination) {
    super(noCheckMd5, overwrite, verbose, edition, database, format);
    this.prefix = s3BucketPrefix;
    this.destination = destination;
    this.s3ClientConfig = s3ClientConfig;
  }

  public DownloadMaxmindS3(
      S3ClientConfig s3ClientConfig,
      boolean noCheckMd5,
      boolean overwrite,
      boolean verbose,
      MaxmindEdition edition,
      MaxmindDatabase database,
      MaxmindFormat format,
      String s3BucketPrefix,
      String destination) {
    this(
        Optional.of(s3ClientConfig),
        noCheckMd5,
        overwrite,
        verbose,
        edition,
        database,
        format,
        s3BucketPrefix,
        destination);
  }

  public DownloadMaxmindS3(
      boolean noCheckMd5,
      boolean overwrite,
      boolean verbose,
      MaxmindEdition edition,
      MaxmindDatabase database,
      MaxmindFormat format,
      String s3BucketPrefix,
      String destination) {
    this(
        Optional.empty(),
        noCheckMd5,
        overwrite,
        verbose,
        edition,
        database,
        format,
        s3BucketPrefix,
        destination);
  }

  @Override
  public DownloadExecutionResult execute() {
    final S3ObjectPath s3ObjectPath = S3ObjectPath.of(prefix);
    S3DownloadSetup downloader = S3DownloadSetup.of(s3ClientConfig);
    final Pair<FileAcceptor, S3Source> download =
        downloader.setupDownload(this.destination, s3ObjectPath, this.maxmindPath());
    return this.process(download.left(), download.right());
  }
}
