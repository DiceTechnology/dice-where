package technology.dice.dicewhere.downloader.commands;

import com.sun.istack.internal.Nullable;
import java.util.Optional;
import technology.dice.dicewhere.downloader.md5.MD5Checksum;

public class CommandExecutionResult {
  private final boolean newFileDownloaded;
  private final Optional<Boolean> md5MatchesSource;
  private final Optional<MD5Checksum> targetFileMd5;
  private final boolean successfull;

  public CommandExecutionResult(
      boolean newFileDownloaded,
      @Nullable Boolean md5MatchesSource,
      @Nullable MD5Checksum targetFileMd5,
      boolean successfull) {
    this.newFileDownloaded = newFileDownloaded;
    this.md5MatchesSource = Optional.ofNullable(md5MatchesSource);
    this.targetFileMd5 = Optional.ofNullable(targetFileMd5);
    this.successfull = successfull;
  }

  public CommandExecutionResult(boolean newFileDownloaded, boolean successfull) {
    this(newFileDownloaded, null, null, successfull);
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
