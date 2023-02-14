package technology.dice.dicewhere.downloader.destination.s3;

import java.io.IOException;
import java.net.URI;
import java.util.Optional;
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
import technology.dice.dicewhere.downloader.actions.S3ClientConfig;
import technology.dice.dicewhere.downloader.destination.FileAcceptor;
import technology.dice.dicewhere.downloader.destination.FileAcceptorFactory;
import technology.dice.dicewhere.downloader.exception.DownloaderException;
import technology.dice.dicewhere.downloader.source.s3.S3Source;

public class S3DownloadSetup {
  private final Optional<S3ClientConfig> s3ClientConfig;

  private S3DownloadSetup(Optional<S3ClientConfig> config) {
    this.s3ClientConfig = config;
  }

  public static S3DownloadSetup of(S3ClientConfig config) {
    return new S3DownloadSetup(Optional.ofNullable(config));
  }

  public static S3DownloadSetup of() {
    return new S3DownloadSetup(Optional.empty());
  }

  public static S3DownloadSetup of(Optional<S3ClientConfig> config) {
    return new S3DownloadSetup(config);
  }

  public Pair<FileAcceptor, S3Source> setupDownload(
      String destination, S3ObjectPath s3ObjectPath, String pathInfix) {
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

    Optional<String> optionalKey = latestKeyForDatabase(s3Client, pathInfix, s3ObjectPath);
    if (!optionalKey.isPresent()) {
      throw new DownloaderException(
          "Could not find the latest version of the database at the source");
    }

    String key = optionalKey.get();

    S3Source s3Source = new S3Source(s3Client, s3ObjectPath.getBucket(), key);

    return Pair.of(
        FileAcceptorFactory.acceptorFor(
            URI.create(destination + "/" + pathInfix + "/" + s3Source.fileInfo().getFileName())),
        s3Source);
  }

  private Optional<String> latestKeyForDatabase(
      S3Client s3Client, String pathInfix, S3ObjectPath s3ObjectPath) {
    GetObjectRequest getObjectRequest =
        GetObjectRequest.builder()
            .bucket(s3ObjectPath.getBucket())
            .key(s3ObjectPath.getPrefix() + pathInfix + "/latest")
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
