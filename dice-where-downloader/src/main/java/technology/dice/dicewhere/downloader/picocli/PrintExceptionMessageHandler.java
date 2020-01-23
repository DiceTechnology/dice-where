package technology.dice.dicewhere.downloader.picocli;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;
import picocli.CommandLine.IExecutionExceptionHandler;
import picocli.CommandLine.ParseResult;

public class PrintExceptionMessageHandler implements IExecutionExceptionHandler {
  private static final Logger LOG = LoggerFactory.getLogger(PrintExceptionMessageHandler.class);

  @Override
  public int handleExecutionException(Exception e, CommandLine cmd, ParseResult parseResult) {
    boolean verbose = false;

    if (cmd.getCommand() instanceof BaseCommand) {
      verbose = ((BaseCommand) cmd.getCommand()).verbose;
    }

    if (verbose) {
      e.printStackTrace();
    }

    LOG.error(e.getMessage());

    return cmd.getExitCodeExceptionMapper() != null
        ? cmd.getExitCodeExceptionMapper().getExitCode(e)
        : cmd.getCommandSpec().exitCodeOnExecutionException();
  }
}
