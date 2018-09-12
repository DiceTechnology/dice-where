package technology.dice.dicewhere.lineprocessing;

import com.google.common.collect.Queues;
import com.google.protobuf.ByteString;
import technology.dice.dicewhere.api.exceptions.LineParsingException;
import technology.dice.dicewhere.lineprocessing.serializers.protobuf.IPInformationProto;
import technology.dice.dicewhere.parsing.LineParser;
import technology.dice.dicewhere.parsing.ParsedLine;
import technology.dice.dicewhere.reading.RawLine;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

public class LineProcessor implements Runnable {
	private static final int WORKER_BATCH_SIZE = 10000;
	private static final int WORKER_COUNT = 4;
	private final ExecutorService executorService;
	private final ArrayBlockingQueue<RawLine> lines;
	private final LineParser parser;
	private final boolean retainOriginalLine;
	private final BlockingQueue<SerializedLine> destination;
	private final LineprocessorListenerForProvider progressListener;
	private boolean expectingMore;

	public LineProcessor(ExecutorService executorService, BlockingQueue<SerializedLine> destination, LineParser parser, boolean retainOriginalLine, LineprocessorListenerForProvider progresListener) {
		this.lines = new ArrayBlockingQueue<>((WORKER_COUNT + 1) * WORKER_BATCH_SIZE);
		this.destination = destination;
		this.executorService = executorService;
		this.parser = parser;
		this.retainOriginalLine = retainOriginalLine;
		this.expectingMore = true;
		this.progressListener = progresListener;
	}

	public int reimainingLines() {
		return this.lines.size();
	}

	public void dontExpectMore() {
		this.expectingMore = false;
	}

	public void addLine(RawLine rawLine) {
		try {
			this.lines.put(rawLine);
		} catch (InterruptedException e) {
			this.progressListener.enqueueError(rawLine, e);
		}
	}

	@Override
	public void run() {
		long started = System.currentTimeMillis();
		AtomicLong totalLines = new AtomicLong();
		CompletableFuture<ArrayList<SerializedLine>>[] workerList = new CompletableFuture[WORKER_COUNT];
		while (this.expectingMore || this.lines.size() > 0) {
			try {
				for (int i = 0; i < WORKER_COUNT; i++) {
					Collection<RawLine> batch = new ArrayList<>(WORKER_BATCH_SIZE);
					Queues.drain(lines, batch, WORKER_BATCH_SIZE, 1, TimeUnit.NANOSECONDS);
					workerList[i] = CompletableFuture.supplyAsync(() -> batch.stream().map(rawLine -> {
								try {
									ParsedLine parsed = parser.parse(rawLine, retainOriginalLine);
									this.progressListener.lineParsed(parsed, System.currentTimeMillis() - started);
									return parsed;
								} catch (LineParsingException e) {
									this.progressListener.parseError(rawLine, e);
									return null;
								}
							})
									.filter(l -> l != null)
									.map(parsedLine -> {
										try {
											IPInformationProto.IpInformationProto.Builder messageBuilder = IPInformationProto.IpInformationProto.newBuilder()
													.setCity(parsedLine.getInfo().getCity().orElse(""))
													.setGeonameId(parsedLine.getInfo().getGeonameId().orElse(""))
													.setCountryCodeAlpha2(parsedLine.getInfo().getCountryCodeAlpha2())
													.setLeastSpecificDivision(parsedLine.getInfo().getLeastSpecificDivision().orElse(""))
													.setMostSpecificDivision(parsedLine.getInfo().getMostSpecificDivision().orElse(""))
													.setPostcode(parsedLine.getInfo().getPostcode().orElse(""))
													.setStartOfRange(ByteString.copyFrom(parsedLine.getStartIp().getBytes()))
													.setEndOfRange(ByteString.copyFrom(parsedLine.getEndIp().getBytes()));

											parsedLine.getInfo().getOriginalLine().ifPresent(originalLine -> messageBuilder.setOriginalLine(originalLine));
											IPInformationProto.IpInformationProto message = messageBuilder.build();


											SerializedLine serializedLine = new SerializedLine(
													parsedLine.getStartIp(),
													message.toByteArray(),
													parsedLine);
											return serializedLine;
										} catch (Exception e) {
											this.progressListener.serializeError(parsedLine, e);
											return null;
										}
									})
									.filter(e -> e != null)
									.collect(Collectors.toCollection(ArrayList::new)),
							executorService);
				}
				CompletableFuture.allOf(workerList);
				for (int i = 0; i < WORKER_COUNT; i++) {
					ArrayList<SerializedLine> join = workerList[i].join();
					join.forEach(serializedLine -> {
								try {
									destination.put(serializedLine);
									totalLines.getAndIncrement();
									this.progressListener.lineProcessed(serializedLine, System.currentTimeMillis() - started);
								} catch (InterruptedException e) {
									this.progressListener.dequeueError(serializedLine, e);
								}
							}
					);
				}
			} catch (InterruptedException e) {
				this.progressListener.processorInterrupted(e);
			}

		}
		this.progressListener.finished(totalLines.get(), System.currentTimeMillis() - started);
	}
}
