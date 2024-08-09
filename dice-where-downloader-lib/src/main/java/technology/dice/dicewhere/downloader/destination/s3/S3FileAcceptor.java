package technology.dice.dicewhere.downloader.destination.s3;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.nio.file.Paths;
import java.time.Clock;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.StorageClass;
import software.amazon.awssdk.utils.StringInputStream;
import technology.dice.dicewhere.downloader.destination.FileAcceptor;
import technology.dice.dicewhere.downloader.md5.MD5Checksum;
import technology.dice.dicewhere.downloader.stream.StreamConsumer;

public class S3FileAcceptor implements FileAcceptor<Void> {

  private static final Logger LOG = LoggerFactory.getLogger(S3FileAcceptor.class);
  private static final String LATEST_KEY = "latest";
  public static final String TIMESTAMP_METADATA_KEY = "ts";
  private final S3Client client;
  private final String bucket;
  private final String key;
  private final Clock clock;
  private final ObjectMapper mapper;

  public S3FileAcceptor(
      S3Client client, String bucket, String key, ObjectMapper mapper, Clock clock) {
    this.client = client;
    this.bucket = bucket;
    this.key = key;
    this.mapper = mapper;
    this.clock = clock;
  }

  @Override
  public StreamConsumer<Void> getStreamConsumer(
      MD5Checksum originalFileMd5, Instant originalFileTimestamp, boolean noMd5Check) {
    return (StreamConsumer)
        (stream, size) -> {
          Map<String, String> objectMetadata = new HashMap<>();
          objectMetadata.put(
              TIMESTAMP_METADATA_KEY, String.valueOf(originalFileTimestamp.toEpochMilli()));
          PutObjectRequest putObjectRequest =
              PutObjectRequest.builder()
                  .key(key)
                  .bucket(bucket)
                  .metadata(objectMetadata)
                  .contentLength(size)
                  .storageClass(StorageClass.INTELLIGENT_TIERING)
                  .build();
          client.putObject(putObjectRequest, RequestBody.fromInputStream(stream, size));
          byte[] buffer = new byte[8192];
          while ((stream.read(buffer)) != -1) {
          }
          Latest latest = new Latest(clock.instant(), key);
          String latestContent = mapper.writeValueAsString(latest);

          if ((!noMd5Check) && (!originalFileMd5.matches(stream.md5()))) {
            LOG.error("MD5 mismatch. Deleting destination file");
            client.deleteObject(DeleteObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build());
          } else {

            PutObjectRequest putLatest =
                PutObjectRequest.builder()
                    .key(Paths.get(key).getParent().toString() + "/" + LATEST_KEY)
                    .bucket(bucket)
                    .contentLength((long) latestContent.length())
                    .storageClass(StorageClass.INTELLIGENT_TIERING)
                    .build();
            client.putObject(
                putLatest,
                RequestBody.fromInputStream(
                    new StringInputStream(latestContent), latestContent.length()));
          }
          return null;
        };
  }

  @Override
  public boolean destinationExists() {
    HeadObjectRequest headObjectRequest =
        HeadObjectRequest.builder().bucket(bucket).key(key).build();

    try {
      client.headObject(headObjectRequest);
      return true;
    } catch (NoSuchKeyException e) {
      return false;
    }
  }

  @Override
  public boolean destinationWritable() {
    return true;
  }

  @Override
  public Optional<MD5Checksum> existingFileMd5() {
    HeadObjectRequest headObjectRequest =
        HeadObjectRequest.builder().bucket(bucket).key(key).build();

    try {
      final HeadObjectResponse headObjectResponse = client.headObject(headObjectRequest);
      return Optional.ofNullable(headObjectResponse.eTag())
          .map(m -> MD5Checksum.of(m.replaceAll("\"", "")));
    } catch (NoSuchKeyException e) {
      return Optional.empty();
    }
  }

  @Override
  public URI getUri() {
    return URI.create("s3://" + bucket + "/" + key);
  }
}
