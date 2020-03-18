package technology.dice.dicewhere.downloader.actions;

import java.net.URI;
import java.util.Optional;
import technology.dice.dicewhere.downloader.md5.MD5Checksum;

public class DownloadExecutionResult {
  private final boolean newFileDownloaded;
  private final Optional<Boolean> md5MatchesSource;
  private final Optional<MD5Checksum> targetFileMd5;
  private final URI fileLocation;
  private final boolean successful;

  public DownloadExecutionResult(
      boolean newFileDownloaded,
      Optional<Boolean> md5MatchesSource,
      Optional<MD5Checksum> targetFileMd5,
      URI fileLocation,
      boolean successful) {
    this.newFileDownloaded = newFileDownloaded;
    this.md5MatchesSource = md5MatchesSource;
    this.targetFileMd5 = targetFileMd5;
    this.successful = successful;
    this.fileLocation = fileLocation;
  }

  public DownloadExecutionResult(
      boolean newFileDownloaded,
      Boolean md5MatchesSource,
      MD5Checksum targetFileMd5,
      URI fileLocation,
      boolean successful) {
    this(
        newFileDownloaded,
        Optional.ofNullable(md5MatchesSource),
        Optional.ofNullable(targetFileMd5),
        fileLocation,
        successful);
  }

  public DownloadExecutionResult(boolean newFileDownloaded, URI fileLocation, boolean successful) {
    this(newFileDownloaded, Optional.empty(), Optional.empty(), fileLocation, successful);
  }

  public boolean isNewFileDownloaded() {
    return newFileDownloaded;
  }

  public URI getFileLocation() {
    return fileLocation;
  }

  public boolean isSuccessful() {
    return successful;
  }

  public Optional<Boolean> getMd5MatchesSource() {
    return md5MatchesSource;
  }

  public Optional<MD5Checksum> getTargetFileMd5() {
    return targetFileMd5;
  }
}
