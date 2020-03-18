package technology.dice.dicewhere.downloader.destination.s3;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;

public class Latest {
  private final Instant uploadedAt;

  private final String key;

  @JsonCreator
  public Latest(@JsonProperty("uploadedAt") Instant uploadedAt, @JsonProperty("key") String key) {
    this.uploadedAt = uploadedAt;
    this.key = key;
  }

  public Instant getUploadedAt() {
    return uploadedAt;
  }

  public String getKey() {
    return key;
  }
}
