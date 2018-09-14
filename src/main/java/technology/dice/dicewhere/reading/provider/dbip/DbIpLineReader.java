package technology.dice.dicewhere.reading.provider.dbip;

import java.io.IOException;
import java.nio.file.Path;
import java.util.stream.Stream;
import technology.dice.dicewhere.parsing.LineParser;
import technology.dice.dicewhere.parsing.provider.DatabaseProvider;
import technology.dice.dicewhere.parsing.provider.dbip.DbIpLineParser;
import technology.dice.dicewhere.reading.LineReader;

public class DbIpLineReader extends LineReader {
  private static final int BUFFER_SIZE = 1024 * 1024;
  private final LineParser lineParser = new DbIpLineParser();
  private final Path csv;

  public DbIpLineReader(Path csv) {
    this.csv = csv;
  }

  @Override
  protected Stream<String> lines() throws IOException {
    return bufferedReaderForPath(csv, BUFFER_SIZE).lines();
  }

  @Override
  public DatabaseProvider provider() {
    return DatabaseProvider.DBIP;
  }

  @Override
  public LineParser parser() {
    return lineParser;
  }
}
