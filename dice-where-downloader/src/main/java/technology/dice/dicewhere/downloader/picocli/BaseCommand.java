package technology.dice.dicewhere.downloader.picocli;

import ch.qos.logback.classic.Level;
import java.util.Optional;
import java.util.concurrent.Callable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine.Option;
import technology.dice.dicewhere.downloader.destination.FileAcceptor;
import technology.dice.dicewhere.downloader.exception.DownloaderException;
import technology.dice.dicewhere.downloader.md5.MD5Checksum;
import technology.dice.dicewhere.downloader.source.FileSource;

public abstract class BaseCommand implements Callable<Integer> {
  private static final Logger LOG = LoggerFactory.getLogger(BaseCommand.class);

  @Option(
      names = {"--no-check-md5"},
      defaultValue = "false",
      description = "Skip md5 checksum of the transferred or destination")
  boolean noCheckMd5;

  @Option(
      names = {"-o", "--overwrite"},
      required = false,
      description =
          "Overwrite destination if it exists. If disabled and md5 check is enabled, the checksum will be performed on the existing file")
  boolean overwrite;

  @Option(names = "-v", required = false, description = "Increases the verbosity of the output.")
  boolean verbose;

  protected int process(FileAcceptor<?, ?> acceptor, FileSource fileSource)
      throws DownloaderException {
    LOG.debug("Acceptor: " + acceptor.getClass().getSimpleName());
    boolean pathWritable = acceptor.destinationWritable();
    LOG.debug("Writable path: " + pathWritable);
    boolean fileExists = acceptor.destinationExists();
    LOG.debug("Existing file: " + fileExists);
    if (fileExists) {

      LOG.info("File exists in " + acceptor.getUri().toString());
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
          return checksumMatches ? 0 : 1;
        } else {
          return 0;
        }
      }
    }

    if (!fileExists) {
      LOG.info("File not found in destination " + acceptor.getUri().toString());
    }

    if ((!fileExists) || overwrite) {
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
          return checksumMatches ? 0 : 1;
        } else {
          return 0;
        }
      } else {
        throw new DownloaderException(
            "Path at " + acceptor.getUri().toString() + " is not writable");
      }
    }
    return 0;
  }

  @Override
  public final Integer call() {
    ch.qos.logback.classic.Logger root =
        (ch.qos.logback.classic.Logger)
            org.slf4j.LoggerFactory.getLogger(ch.qos.logback.classic.Logger.ROOT_LOGGER_NAME);
    if (verbose) {
      root.setLevel(Level.DEBUG);
    }
    this.checkNecessaryEnvironmentVariables();
    return this.execute();
  }

  protected abstract int execute();

  protected String[] necessaryEnvironmentVariables() {
    return new String[] {};
  }

  protected void checkNecessaryEnvironmentVariables() {}
}