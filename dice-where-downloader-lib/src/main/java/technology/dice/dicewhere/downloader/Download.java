package technology.dice.dicewhere.downloader;

import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import technology.dice.dicewhere.downloader.actions.DownloadExecutionResult;
import technology.dice.dicewhere.downloader.destination.FileAcceptor;
import technology.dice.dicewhere.downloader.exception.DownloaderException;
import technology.dice.dicewhere.downloader.md5.MD5Checksum;
import technology.dice.dicewhere.downloader.source.FileSource;

public abstract class Download {
  private static final Logger LOG = LoggerFactory.getLogger(Download.class);

  protected final boolean noCheckMd5;
  protected final boolean overwrite;
  protected final boolean verbose;

  public Download(boolean noCheckMd5, boolean overwrite, boolean verbose) {
    this.noCheckMd5 = noCheckMd5;
    this.overwrite = overwrite;
    this.verbose = verbose;
  }

  protected DownloadExecutionResult process(FileAcceptor<?> acceptor, FileSource fileSource)
      throws DownloaderException {
    LOG.debug("Acceptor: " + acceptor.getClass().getSimpleName());
    boolean pathWritable = acceptor.destinationWritable();
    LOG.debug("Writable path: " + pathWritable);
    boolean fileExists = acceptor.destinationExists();
    LOG.debug("Existing file: " + fileExists);
    if (fileExists) {
      LOG.info("File exists in " + acceptor.getUri().toString());
      return processFileExists(acceptor, fileSource, pathWritable);
    } else {
      LOG.info("File not found in destination " + acceptor.getUri().toString());
      return processFileDoesNotExist(acceptor, fileSource, pathWritable);
    }
  }

  private DownloadExecutionResult processFileDoesNotExist(
      FileAcceptor<?> acceptor, FileSource fileSource, boolean pathWritable) {

    if (pathWritable) {
      final MD5Checksum md5Checksum = fileSource.produce(acceptor);
      LOG.info("File successfully transferred");
      if (!noCheckMd5) {
        boolean checksumMatches = md5Checksum.matches(fileSource.fileInfo().getMd5Checksum());
        if (!checksumMatches) {
          LOG.warn(
              "Local and remote files' MD5 do not match: "
                  + md5Checksum.stringFormat()
                  + " Vs. "
                  + fileSource.fileInfo().getMd5Checksum().stringFormat());
        } else {
          LOG.info("MD5 matches that of the remote file");
        }
        return new DownloadExecutionResult(
            true, checksumMatches, md5Checksum, acceptor.getUri(), checksumMatches);
      } else {
        return new DownloadExecutionResult(true, acceptor.getUri(), true);
      }
    } else {
      throw new DownloaderException("Path at " + acceptor.getUri().toString() + " is not writable");
    }
  }

  private DownloadExecutionResult processFileExists(
      FileAcceptor<?> acceptor, FileSource fileSource, boolean pathWritable) {
    if (overwrite) {
      LOG.info("Overwrite option is enabled. Destination will be overwritten");
    }
    if (!overwrite) {
      if (!noCheckMd5) {
        Optional<MD5Checksum> existingMd5 = acceptor.existingFileMd5();
        boolean checksumMatches =
            existingMd5
                .map(md5 -> md5.matches(fileSource.fileInfo().getMd5Checksum()))
                .orElse(false);
        if (!checksumMatches) {
          LOG.warn(
              "Local and remote files' MD5 do not match: "
                  + existingMd5.map(md5 -> md5.stringFormat()).orElse("?")
                  + " Vs. "
                  + fileSource.fileInfo().getMd5Checksum().stringFormat());
        } else {
          LOG.info("MD5 matches that of the remote file");
        }
        return new DownloadExecutionResult(
            true,
            existingMd5.map(unused -> checksumMatches).orElse(null),
            existingMd5.orElse(null),
            acceptor.getUri(),
            checksumMatches);
      } else {
        return new DownloadExecutionResult(
            false, Optional.empty(), Optional.empty(), acceptor.getUri(), true);
      }
    } else {
      return this.processFileDoesNotExist(acceptor, fileSource, pathWritable);
    }
  }

  protected abstract DownloadExecutionResult execute();

  protected void checkNecessaryEnvironmentVariables() {}

  public boolean isVerbose() {
    return verbose;
  }
}
