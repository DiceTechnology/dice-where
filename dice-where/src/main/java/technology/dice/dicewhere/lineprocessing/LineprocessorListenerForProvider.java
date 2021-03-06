/*
 * Copyright (C) 2018 - present by Dice Technology Ltd.
 *
 * Please see distribution for license.
 */

package technology.dice.dicewhere.lineprocessing;

import java.util.Objects;
import technology.dice.dicewhere.api.exceptions.LineParsingException;
import technology.dice.dicewhere.parsing.ParsedLine;
import technology.dice.dicewhere.provider.ProviderKey;
import technology.dice.dicewhere.reading.RawLine;

public class LineprocessorListenerForProvider {
  private final ProviderKey provider;
  private final LineProcessorListener listener;

  public LineprocessorListenerForProvider(
      ProviderKey provider, LineProcessorListener lineProcessingExceptionListener) {
    this.provider = Objects.requireNonNull(provider);
    this.listener = Objects.requireNonNull(lineProcessingExceptionListener);
  }

  public void enqueueError(RawLine rawLine, Exception e) {
    listener.enqueueError(provider, rawLine, e);
  }

  public void parseError(RawLine rawLine, LineParsingException e) {
    listener.parseError(provider, rawLine, e);
  }

  public void serializeError(ParsedLine parsedLine, Exception e) {
    listener.serializeError(provider, parsedLine, e);
  }

  public void dequeueError(SerializedLine serializedLine, Exception e) {
    listener.dequeueError(provider, serializedLine, e);
  }

  public void processorInterrupted(InterruptedException e) {
    listener.processorInterrupted(provider, e);
  }

  public void lineProcessed(SerializedLine serializedLine, long timeElapsed) {
    listener.lineProcessed(provider, serializedLine, timeElapsed);
  }

  public void lineParsed(ParsedLine parsed, long timeElapsed) {
    listener.lineParsed(provider, parsed, timeElapsed);
  }

  void finished(long totalLines, long timeElapsed) {
    listener.finished(provider, totalLines, timeElapsed);
  }
}
