package technology.dice.dicewhere.downloader.actions.ipinfo;

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
import technology.dice.dicewhere.downloader.ObjectMapperInstance;
import technology.dice.dicewhere.downloader.actions.DownloadExecutionResult;
import technology.dice.dicewhere.downloader.actions.S3ClientConfig;
import technology.dice.dicewhere.downloader.destination.FileAcceptor;
import technology.dice.dicewhere.downloader.destination.FileAcceptorFactory;
import technology.dice.dicewhere.downloader.destination.s3.Latest;
import technology.dice.dicewhere.downloader.destination.s3.ObjectPath;
import technology.dice.dicewhere.downloader.exception.DownloaderException;
import technology.dice.dicewhere.downloader.source.s3.S3Source;

public class DownloadIpInfoS3 extends IpInfoBaseDownload {
  private static final Logger LOG = LoggerFactory.getLogger(DownloadIpInfoS3.class);
  private final Optional<S3ClientConfig> s3ClientConfig;

  private final String prefix;
  private final String destination;

  private DownloadIpInfoS3(
      Optional<S3ClientConfig> s3ClientConfig,
      boolean noCheckMd5,
      boolean overwrite,
      boolean verbose,
      IpInfoDataset dataset,
      IpInfoFormat format,
      String s3BucketPrefix,
      String destination) {
    super(noCheckMd5, overwrite, verbose, dataset, format);
    this.prefix = s3BucketPrefix;
    this.destination = destination;
    this.s3ClientConfig = s3ClientConfig;
  }

  public DownloadIpInfoS3(
      S3ClientConfig s3ClientConfig,
      boolean noCheckMd5,
      boolean overwrite,
      boolean verbose,
      IpInfoDataset dataset,
      IpInfoFormat format,
      String s3BucketPrefix,
      String destination) {
    this(
        Optional.of(s3ClientConfig),
        noCheckMd5,
        overwrite,
        verbose,
        dataset,
        format,
        s3BucketPrefix,
        destination);
  }

  public DownloadIpInfoS3(
      boolean noCheckMd5,
      boolean overwrite,
      boolean verbose,
      IpInfoDataset dataset,
      IpInfoFormat format,
      String s3BucketPrefix,
      String destination) {
    this(
        Optional.empty(),
        noCheckMd5,
        overwrite,
        verbose,
        dataset,
        format,
        s3BucketPrefix,
        destination);
  }

  @Override
  public DownloadExecutionResult execute() {
    final ObjectPath objectPath = ObjectPath.of(prefix);
    S3Client s3Client =
        this.s3ClientConfig
            .map(
                c ->
                    S3Client.builder()
                        .credentialsProvider(
                            StaticCredentialsProvider.create(
                                AwsBasicCredentials.create(c.getAwsKeyId(), c.getAwsSecretKey())))
                        .region(Region.of(c.getAwsRegion()))
                        .build())
            .orElseGet(() -> S3Client.create());
    Optional<String> optionalKey = latestKeyForDatabase(s3Client, objectPath);
    if (!optionalKey.isPresent()) {
      throw new DownloaderException(
          "Could not find the latest version of the database at the source");
    }

    String key = optionalKey.get();

    S3Source s3Source = new S3Source(s3Client, objectPath.getBucket(), key);

    FileAcceptor<?> acceptor =
        FileAcceptorFactory.acceptorFor(
            URI.create(
                this.destination
                    + "/"
                    + this.ipInfoPath()
                    + "/"
                    + s3Source.fileInfo().getFileName()));

    return this.process(acceptor, s3Source);
  }

  private Optional<String> latestKeyForDatabase(S3Client s3Client, ObjectPath objectPath) {
    GetObjectRequest getObjectRequest =
        GetObjectRequest.builder()
            .bucket(objectPath.getBucket())
            .key(objectPath.getPrefix() + this.ipInfoPath() + "/latest")
            .build();
    try (final ResponseInputStream<GetObjectResponse> object =
        s3Client.getObject(getObjectRequest); ) {
      final Latest latest = ObjectMapperInstance.INSTANCE.readValue(object, Latest.class);
      return Optional.ofNullable(latest.getKey());
    } catch (IOException | NoSuchKeyException e) {
      throw new DownloaderException("Latest version information not readable for at the source");
    }
  }
}
