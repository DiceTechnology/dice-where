package technology.dice.dicewhere.downloader.commands;

import java.util.Optional;
import technology.dice.dicewhere.downloader.md5.MD5Checksum;

public class CommandExecutionResult {
  private final boolean newFileDownloaded;
  private final Optional<Boolean> md5MatchesSource;
  private final Optional<MD5Checksum> targetFileMd5;
  private final boolean successfull;

  public CommandExecutionResult(
      boolean newFileDownloaded,
      Optional<Boolean> md5MatchesSource,
      Optional<MD5Checksum> targetFileMd5,
      boolean successfull) {
    this.newFileDownloaded = newFileDownloaded;
    this.md5MatchesSource = md5MatchesSource;
    this.targetFileMd5 = targetFileMd5;
    this.successfull = successfull;
  }

  public CommandExecutionResult(
      boolean newFileDownloaded,
      Boolean md5MatchesSource,
      MD5Checksum targetFileMd5,
      boolean successfull) {
    this.newFileDownloaded = newFileDownloaded;
    this.md5MatchesSource = Optional.ofNullable(md5MatchesSource);
    this.targetFileMd5 = Optional.ofNullable(targetFileMd5);
    this.successfull = successfull;
  }

  public CommandExecutionResult(boolean newFileDownloaded, boolean successfull) {
    this(newFileDownloaded, Optional.empty(), Optional.empty(), successfull);
  }

  public boolean isNewFileDownloaded() {
    return newFileDownloaded;
  }

  public boolean isSuccessfull() {
    return successfull;
  }

  public Optional<Boolean> getMd5MatchesSource() {
    return md5MatchesSource;
  }

  public Optional<MD5Checksum> getTargetFileMd5() {
    return targetFileMd5;
  }
}
