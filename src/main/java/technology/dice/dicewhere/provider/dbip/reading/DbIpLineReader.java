package technology.dice.dicewhere.provider.dbip.reading;

import java.io.IOException;
import java.nio.file.Path;
import java.util.stream.Stream;
import technology.dice.dicewhere.parsing.LineParser;
import technology.dice.dicewhere.provider.ProviderKey;
import technology.dice.dicewhere.provider.dbip.DbIpProviderKey;
import technology.dice.dicewhere.provider.dbip.parsing.DbIpIpToLocationAndIspCSVLineParser;
import technology.dice.dicewhere.reading.LineReader;

public class DbIpLineReader extends LineReader {
  private static final int BUFFER_SIZE = 1024 * 1024;
  private final LineParser lineParser = new DbIpIpToLocationAndIspCSVLineParser();
  private final Path csv;

  public DbIpLineReader(Path csv) {
    this.csv = csv;
  }

  @Override
  protected Stream<String> lines() throws IOException {
    return bufferedReaderforPath(csv, BUFFER_SIZE).lines();
  }

  @Override
  public ProviderKey provider() {
    return DbIpProviderKey.of();
  }

  @Override
  public LineParser parser() {
    return lineParser;
  }
}
