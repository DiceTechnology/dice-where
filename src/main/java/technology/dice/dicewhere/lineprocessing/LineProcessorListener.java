package technology.dice.dicewhere.lineprocessing;

import technology.dice.dicewhere.api.exceptions.LineParsingException;
import technology.dice.dicewhere.parsing.ParsedLine;
import technology.dice.dicewhere.provider.ProviderKey;
import technology.dice.dicewhere.reading.RawLine;

public interface LineProcessorListener {
  default void enqueueError(ProviderKey provider, RawLine rawLine, Exception e) {
    throw new RuntimeException(e);
  }

  default void serializeError(ProviderKey provider, ParsedLine parsedLine, Exception e) {
    throw new RuntimeException(e);
  }

  default void dequeueError(ProviderKey provider, SerializedLine serializedLine, Exception e) {
    throw new RuntimeException(e);
  }

  default void processorInterrupted(ProviderKey provider, InterruptedException e) {
    throw new RuntimeException(e);
  }

  default void parseError(ProviderKey provider, RawLine rawLine, LineParsingException e) {
    throw new RuntimeException(e);
  }

  default void lineProcessed(
      ProviderKey provider, SerializedLine serializedLine, long timeElapsed) {}

  default void lineParsed(ProviderKey provider, ParsedLine parsed, long timeElapsed) {}

  default void finished(ProviderKey provider, long totalLines, long timeElapsed) {}
}
