package technology.dice.dicewhere.downloader.files;

import java.net.URI;
import java.nio.file.Paths;
import java.time.Instant;
import technology.dice.dicewhere.downloader.md5.MD5Checksum;

public class S3FileInfo implements FileInfo {
  private final String bucket;
  private final String key;
  private final MD5Checksum md5;
  private final long size;
  private final Instant timestamp;

  public S3FileInfo(String bucket, String key, Instant timestamp, MD5Checksum md5, long size) {
    this.bucket = bucket;
    this.key = key;
    this.md5 = md5;
    this.size = size;
    this.timestamp = timestamp;
  }

  @Override
  public MD5Checksum getMd5Checksum() {
    return this.md5;
  }

  @Override
  public URI getUri() {
    return URI.create("s3://" + bucket + "/" + key);
  }

  @Override
  public long getSize() {
    return this.size;
  }

  @Override
  public String getFileName() {
    return Paths.get(this.key).getFileName().toString();
  }

  @Override
  public Instant getTimestamp() {
    return timestamp;
  }
}
