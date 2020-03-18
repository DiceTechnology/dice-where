package technology.dice.dicewhere.downloader.actions;

import java.util.Objects;

public final class S3ClientConfig {
  private final String awsKeyId;
  private final String awsSecretKey;
  private final String awsRegion;

  public S3ClientConfig(String awsKeyId, String awsSecretKey, String awsRegion) {
    Objects.nonNull(awsKeyId);
    Objects.nonNull(awsSecretKey);
    Objects.nonNull(awsRegion);
    this.awsKeyId = awsKeyId;
    this.awsSecretKey = awsSecretKey;
    this.awsRegion = awsRegion;
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
}
