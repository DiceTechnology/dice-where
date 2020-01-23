package technology.dice.dicewhere.downloader.destination.s3;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.nio.file.Paths;
import java.time.Clock;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.StorageClass;
import software.amazon.awssdk.utils.StringInputStream;
import technology.dice.dicewhere.downloader.destination.FileAcceptor;
import technology.dice.dicewhere.downloader.md5.MD5Checksum;
import technology.dice.dicewhere.downloader.stream.StreamConsumer;

public class S3FileAcceptor implements FileAcceptor<Void, MD5Checksum> {

  private static final String LATEST_KEY = "latest";
  public static final String MD5_METADATA_KEY = "md5";
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
  public StreamConsumer<Void> getStreamConsumer(Optional<MD5Checksum> checksum) {
    return (StreamConsumer)
        (stream, size) -> {
          Map<String, String> objectMetadata = new HashMap<>();
          checksum.ifPresent(c -> objectMetadata.put(MD5_METADATA_KEY, c.stringFormat()));
          PutObjectRequest putObjectRequest =
              PutObjectRequest.builder()
                  .key(key)
                  .bucket(bucket)
                  .metadata(objectMetadata)
                  .contentLength(size)
                  .storageClass(StorageClass.INTELLIGENT_TIERING)
                  .build();
          client.putObject(putObjectRequest, RequestBody.fromInputStream(stream, size));

          Latest latest = new Latest(clock.instant(), key);
          String latestContent = mapper.writeValueAsString(latest);

          PutObjectRequest putLatest =
              PutObjectRequest.builder()
                  .key(Paths.get(key).getParent().toString() + "/" + LATEST_KEY)
                  .bucket(bucket)
                  .contentLength(new Long(latestContent.length()))
                  .storageClass(StorageClass.INTELLIGENT_TIERING)
                  .build();
          client.putObject(
              putLatest,
              RequestBody.fromInputStream(
                  new StringInputStream(latestContent), latestContent.length()));

          return null;
        };
  }

  @Override
  public boolean destinationExists() {
    HeadObjectRequest headObjectRequest =
        HeadObjectRequest.builder().bucket(bucket).key(key).build();

    try {
      client.headObject(headObjectRequest);
    } catch (NoSuchKeyException e) {
      return false;
    }
    return true;
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
      final Map<String, String> metadata = headObjectResponse.metadata();
      return Optional.ofNullable(metadata.get(MD5_METADATA_KEY)).map(m -> MD5Checksum.of(m));
    } catch (NoSuchKeyException e) {
      return Optional.empty();
    }
  }

  @Override
  public URI getUri() {
    return URI.create("s3://" + bucket + "/" + key);
  }
}
