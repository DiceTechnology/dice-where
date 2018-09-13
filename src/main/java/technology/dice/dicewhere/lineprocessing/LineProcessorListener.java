package technology.dice.dicewhere.lineprocessing;

import technology.dice.dicewhere.api.exceptions.LineParsingException;
import technology.dice.dicewhere.parsing.ParsedLine;
import technology.dice.dicewhere.parsing.provider.DatabaseProvider;
import technology.dice.dicewhere.reading.RawLine;

public interface LineProcessorListener {
	default void enqueueError(DatabaseProvider provider, RawLine rawLine, Exception e) {
		throw new RuntimeException(e);
	}

	default void serializeError(DatabaseProvider provider, ParsedLine parsedLine, Exception e) {
		throw new RuntimeException(e);
	}

	default void dequeueError(DatabaseProvider provider, SerializedLine serializedLine, Exception e) {
		throw new RuntimeException(e);
	}

	default void processorInterrupted(DatabaseProvider provider, InterruptedException e) {
		throw new RuntimeException(e);
	}

	default void parseError(DatabaseProvider provider, RawLine rawLine, LineParsingException e) {
		throw new RuntimeException(e);
	}

	default void lineProcessed(DatabaseProvider provider, SerializedLine serializedLine, long timeElapsed) {

	}

	default void lineParsed(DatabaseProvider provider, ParsedLine parsed, long timeElapsed) {

	}

	default void finished(DatabaseProvider provider, long totalLines, long timeElapsed) {

	}
}
