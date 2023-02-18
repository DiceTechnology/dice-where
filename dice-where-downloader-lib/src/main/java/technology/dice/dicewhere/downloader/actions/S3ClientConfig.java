package technology.dice.dicewhere.downloader.actions;

import java.util.Objects;
import java.util.Optional;

public final class S3ClientConfig {
  private final String awsKeyId;
  private final String awsSecretKey;
  private final String awsRegion;
  private final Optional<String> endpoint;

  public S3ClientConfig(
      String awsKeyId, String awsSecretKey, String awsRegion, Optional<String> endpoint) {
    Objects.nonNull(awsKeyId);
    Objects.nonNull(awsSecretKey);
    Objects.nonNull(awsRegion);
    this.awsKeyId = awsKeyId;
    this.awsSecretKey = awsSecretKey;
    this.awsRegion = awsRegion;
    this.endpoint = endpoint;
  }

  public S3ClientConfig(String awsKeyId, String awsSecretKey, String awsRegion, String endpoint) {
    this(awsKeyId, awsSecretKey, awsSecretKey, Optional.ofNullable(endpoint));
  }

  public S3ClientConfig(String awsKeyId, String awsSecretKey, String awsRegion) {
    this(awsKeyId, awsSecretKey, awsRegion, Optional.empty());
  }

  public String getAwsKeyId() {
    return awsKeyId;
  }

  public String getAwsSecretKey() {
    return awsSecretKey;
  }

  public String getAwsRegion() {
    return awsRegion;
  }

  public Optional<String> getEndpoint() {
    return endpoint;
  }
}
