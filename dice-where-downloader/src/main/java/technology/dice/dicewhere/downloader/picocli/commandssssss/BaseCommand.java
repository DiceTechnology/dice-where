package technology.dice.dicewhere.downloader.picocli.commandssssss;

import ch.qos.logback.classic.Level;
import java.util.concurrent.Callable;
import picocli.CommandLine.Option;
import technology.dice.dicewhere.downloader.actions.DownloadExecutionResult;

public abstract class BaseCommand implements Callable<Integer> {

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

  @Override
  public final Integer call() {
    ch.qos.logback.classic.Logger root =
        (ch.qos.logback.classic.Logger)
            org.slf4j.LoggerFactory.getLogger(ch.qos.logback.classic.Logger.ROOT_LOGGER_NAME);
    if (verbose) {
      root.setLevel(Level.DEBUG);
    }
    this.checkNecessaryEnvironmentVariables();
    return this.execute().isSuccessful() ? 0 : 1;
  }

  protected abstract DownloadExecutionResult execute();

  protected void checkNecessaryEnvironmentVariables() {}

  public boolean isVerbose() {
    return verbose;
  }
}
