/*
 * Copyright (C) 2018 - present by Dice Technology Ltd.
 *
 * Please see distribution for license.
 */

package technology.dice.dicewhere.lineprocessing;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Queues;
import com.google.protobuf.ByteString;
import technology.dice.dicewhere.api.exceptions.LineParsingException;
import technology.dice.dicewhere.lineprocessing.serializers.protobuf.IPInformationProto.IpInformationProto;
import technology.dice.dicewhere.parsing.LineParser;
import technology.dice.dicewhere.parsing.ParsedLine;
import technology.dice.dicewhere.reading.RawLine;
import technology.dice.dicewhere.utils.ProtoValueConverter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;

/**
 * Responsible for processing the lines from a db provider's files and putting them in a serialized
 * form.
 */
public class LineProcessor implements Runnable {

  private static final int WORKER_BATCH_SIZE = 10000;
  private final ExecutorService executorService;
  private final ArrayBlockingQueue<RawLine> lines;
  private final LineParser parser;
  private final boolean retainOriginalLine;
  private final BlockingQueue<SerializedLine> destination;
  private final LineprocessorListenerForProvider progressListener;
  private final AtomicBoolean expectingMore = new AtomicBoolean(true);
  private final int workersCount;

  /**
   * @param executorService the executor service used for handling parsing of batches
   * @param destination the queue where serialized lines are written to
   * @param parser the parser to use for parsing the line data
   * @param retainOriginalLine indicates if the original line data should be retained alongside the
   *     serialized data
   * @param progressListener the listener for reporting progress
   */
  public LineProcessor(
      ExecutorService executorService,
      BlockingQueue<SerializedLine> destination,
      LineParser parser,
      boolean retainOriginalLine,
      LineprocessorListenerForProvider progressListener,
      int workersCount) {
    this.lines = new ArrayBlockingQueue<>((workersCount + 1) * WORKER_BATCH_SIZE);
    this.destination = destination;
    this.executorService = executorService;
    this.parser = parser;
    this.retainOriginalLine = retainOriginalLine;
    this.progressListener = progressListener;
    this.workersCount = workersCount;
  }

  /**
   * Marks that the no more data should be expected, though in flight data will still be processed.
   */
  public void markDataComplete() {
    expectingMore.set(false);
  }

  /**
   * Add a new line of raw data for parsing and serialization
   *
   * @param rawLine the raw line data
   */
  public void addLine(RawLine rawLine) {
    try {
      lines.put(rawLine);
    } catch (InterruptedException e) {
      progressListener.enqueueError(rawLine, e);
    }
  }

  /** Runs the processor, parsing the raw line data and serializing it into a suitable form. */
  @Override
  public void run() {
    long started = System.currentTimeMillis();

    AtomicLong totalLines = new AtomicLong();
    CompletableFuture<List<SerializedLine>>[] workerList = new CompletableFuture[workersCount];

    while (expectingMore.get() || lines.size() > 0) {
      try {
        for (int i = 0; i < workersCount; i++) {
          Collection<RawLine> batch = new ArrayList<>(WORKER_BATCH_SIZE);

          // Populate the batch from the lines queue
          Queues.drain(lines, batch, WORKER_BATCH_SIZE, 1, TimeUnit.NANOSECONDS);
          workerList[i] =
              CompletableFuture.supplyAsync(
                  () -> buildSerializedLineBatch(started, batch), executorService);
        }

        CompletableFuture.allOf(workerList);

        for (CompletableFuture<List<SerializedLine>> worker : workerList) {
          worker
              .join()
              .forEach(
                  serializedLine -> {
                    try {
                      destination.put(serializedLine);
                      totalLines.getAndIncrement();
                      progressListener.lineProcessed(
                          serializedLine, System.currentTimeMillis() - started);
                    } catch (InterruptedException e) {
                      progressListener.dequeueError(serializedLine, e);
                    }
                  });
        }

      } catch (InterruptedException e) {
        progressListener.processorInterrupted(e);
        throw new RuntimeException("Line processor interrupted", e);
      }
    }

    progressListener.finished(totalLines.get(), System.currentTimeMillis() - started);
  }

  private ImmutableList<SerializedLine> buildSerializedLineBatch(
      long started, Collection<RawLine> batch) {
    return batch
        .stream()
        .flatMap(rawline -> attemptParse(rawline, started))
        .collect(ImmutableList.toImmutableList());
  }

  private Stream<SerializedLine> attemptParse(RawLine rawLine, long started) {
    try {
      Stream<ParsedLine> parsed = parser.parse(rawLine, retainOriginalLine);
      long now = System.currentTimeMillis();
      return parsed.flatMap(
          l -> {
            progressListener.lineParsed(l, now - started);
            return attemptSerialize(l);
          });
    } catch (LineParsingException e) {
      progressListener.parseError(rawLine, e);
      return Stream.empty();
    } catch (Exception e) {
      progressListener.parseError(rawLine, new LineParsingException(e, rawLine));
      return Stream.empty();
    }
  }

  private Stream<SerializedLine> attemptSerialize(ParsedLine parsedLine) {
    try {
      IpInformationProto message = createIpProtobuf(parsedLine);

      return Stream.of(
          new SerializedLine(parsedLine.getStartIp(), message.toByteArray(), parsedLine));

    } catch (Exception e) {
      progressListener.serializeError(parsedLine, e);
      return Stream.empty();
    }
  }

  private IpInformationProto createIpProtobuf(ParsedLine parsedLine) {
    IpInformationProto.Builder messageBuilder =
        IpInformationProto.newBuilder()
            .setCity(parsedLine.getInfo().getCity().orElse(""))
            .setGeonameId(parsedLine.getInfo().getGeonameId().orElse(""))
            .setCountryCodeAlpha2(parsedLine.getInfo().getCountryCodeAlpha2())
            .setLeastSpecificDivision(parsedLine.getInfo().getLeastSpecificDivision().orElse(""))
            .setMostSpecificDivision(parsedLine.getInfo().getMostSpecificDivision().orElse(""))
            .setPostcode(parsedLine.getInfo().getPostcode().orElse(""))
            .setStartOfRange(ByteString.copyFrom(parsedLine.getStartIp().getBytes()))
            .setEndOfRange(ByteString.copyFrom(parsedLine.getEndIp().getBytes()))
            .setIsVpn(
                ProtoValueConverter.toThreeStateValue(parsedLine.getInfo().isVpn().orElse(null)));

    parsedLine.getInfo().getOriginalLine().ifPresent(messageBuilder::setOriginalLine);

    return messageBuilder.build();
  }
}
