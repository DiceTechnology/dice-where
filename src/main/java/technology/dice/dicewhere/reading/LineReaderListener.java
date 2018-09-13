package technology.dice.dicewhere.reading;

import technology.dice.dicewhere.parsing.provider.DatabaseProvider;

public interface LineReaderListener {
  default void lineRead(DatabaseProvider provider, RawLine rawLine, long elapsedMillis) {}

  default void finished(DatabaseProvider provider, long linesProcessed, long elapsedMillis) {}
}
