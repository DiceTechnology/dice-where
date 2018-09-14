package technology.dice.dicewhere.reading;

import technology.dice.dicewhere.provider.ProviderKey;

public interface LineReaderListener {
  default void lineRead(ProviderKey provider, RawLine rawLine, long elapsedMillis) {}

  default void finished(ProviderKey provider, long linesProcessed, long elapsedMillis) {}
}
