package technology.dice.dicewhere.downloader.source.s3;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;
import technology.dice.dicewhere.downloader.destination.FileAcceptor;
import technology.dice.dicewhere.downloader.exception.DownloaderException;
import technology.dice.dicewhere.downloader.files.FileInfo;
import technology.dice.dicewhere.downloader.files.S3FileInfo;
import technology.dice.dicewhere.downloader.md5.MD5Checksum;
import technology.dice.dicewhere.downloader.source.FileSource;
import technology.dice.dicewhere.downloader.stream.StreamWithMD5Decorator;

public class S3Source implements FileSource {
  private static Logger LOG = LoggerFactory.getLogger(S3Source.class);
  public static final String MD5_METADATA_KEY = "md5";
  public static final String TIMESTAMP_METADATA_KEY = "ts";
  private final S3Client client;
  private final String bucket;
  private final String key;
  private FileInfo fileInfo;

  public S3Source(S3Client s3Client, String bucket, String key) {
    this.client = s3Client;
    this.bucket = bucket;
    this.key = key;
  }

  @Override
  public FileInfo fileInfo() {
    if (this.fileInfo == null) {
      HeadObjectRequest headObjectRequest =
          HeadObjectRequest.builder().key(key).bucket(bucket).build();

      final HeadObjectResponse headObjectResponse = client.headObject(headObjectRequest);
      final Map<String, String> metadata = headObjectResponse.metadata();
      if (!metadata.containsKey(MD5_METADATA_KEY)) {
        throw new DownloaderException(
            "Remote file does not have md5 information. Please delete the file and re-upload");
      }
      if (!metadata.containsKey(TIMESTAMP_METADATA_KEY)) {
        LOG.warn("Timestamp not available at source. Using now as timestamp.");
      }
      long size = headObjectResponse.contentLength();
      this.fileInfo =
          new S3FileInfo(
              bucket,
              key,
              Optional.ofNullable(metadata.get(TIMESTAMP_METADATA_KEY))
                  .map(m -> Instant.ofEpochMilli(Long.valueOf(m)))
                  .orElse(Instant.now()),
              MD5Checksum.of(metadata.get(MD5_METADATA_KEY)),
              size);
    }

    return this.fileInfo;
  }

  @Override
  public MD5Checksum produce(FileAcceptor consumer) {
    GetObjectRequest getObjectRequest = GetObjectRequest.builder().bucket(bucket).key(key).build();
    try (final ResponseInputStream<GetObjectResponse> object = client.getObject(getObjectRequest);
        StreamWithMD5Decorator is = StreamWithMD5Decorator.of(object)) {
      consumer
          .getStreamConsumer(fileInfo.getMd5Checksum(), fileInfo.getTimestamp())
          .consume(is, fileInfo.getSize());
      return is.md5();
    } catch (IOException | NoSuchAlgorithmException e) {
      throw new DownloaderException("Could not read file at " + fileInfo.getUri().toString(), e);
    }
  }
}
