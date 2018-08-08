package technology.dice.dicewhere.lineprocessing;


import technology.dice.dicewhere.api.exceptions.LineParsingException;
import technology.dice.dicewhere.parsing.ParsedLine;
import technology.dice.dicewhere.parsing.provider.DatabaseProvider;
import technology.dice.dicewhere.reading.RawLine;

import java.util.Objects;

public class LineprocessorListenerForProvider {
	private final DatabaseProvider provider;
	private final LineProcessorListener listener;

	public LineprocessorListenerForProvider(DatabaseProvider provider, LineProcessorListener lineProcessingExceptionListener) {
		this.provider = Objects.requireNonNull(provider);
		this.listener = Objects.requireNonNull(lineProcessingExceptionListener);
	}

	public void enqueueError(RawLine rawLine, Exception e) {
		this.listener.enqueueError(this.provider, rawLine, e);
	}

	public void parseError(RawLine rawLine, LineParsingException e) {
		this.listener.parseError(this.provider, rawLine, e);
	}

	public void serializeError(ParsedLine parsedLine, Exception e) {
		this.listener.serializeError(this.provider, parsedLine, e);
	}

	public void dequeueError(SerializedLine serializedLine, Exception e) {
		this.listener.dequeueError(this.provider, serializedLine, e);
	}

	public void processorInterrupted(InterruptedException e) {
		this.listener.processorInterrupted(this.provider, e);
	}

	public void lineProcessed(SerializedLine serializedLine, long timeElapsed) {
		this.listener.lineProcessed(this.provider, serializedLine, timeElapsed);
	}

	public void lineParsed(ParsedLine parsed, long timeElapsed) {
		this.listener.lineParsed(this.provider, parsed, timeElapsed);
	}

	void finished(long totalLines, long timeElapsed) {
		this.listener.finished(this.provider, totalLines, timeElapsed);
	}
}
