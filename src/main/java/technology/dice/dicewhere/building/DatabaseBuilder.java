package technology.dice.dicewhere.building;


import com.google.common.collect.Queues;
import org.mapdb.DB;
import org.mapdb.DBException;
import org.mapdb.DBMaker;
import org.mapdb.Serializer;
import technology.dice.dicewhere.api.api.IP;
import technology.dice.dicewhere.lineprocessing.SerializedLine;
import technology.dice.dicewhere.lineprocessing.serializers.IPSerializer;
import technology.dice.dicewhere.parsing.provider.DatabaseProvider;

import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

public class DatabaseBuilder implements Runnable {
	private final ArrayBlockingQueue<SerializedLine> source;
	private final DatabaseBuilderListener listener;
	private final DatabaseProvider provider;
	private boolean expectingMore;
	private final DB.TreeMapSink<IP, byte[]> sink;
	private int processedLines = 0;

	public DatabaseBuilder(DatabaseProvider provider, ArrayBlockingQueue<SerializedLine> source, DatabaseBuilderListener listener) {
		this.source = source;
		this.expectingMore = true;
		this.listener = listener;
		this.provider = provider;
		DB db = DBMaker.tempFileDB()
				.checksumHeaderBypass()
				.fileLockDisable()
				.fileMmapEnable()
				.fileChannelEnable()
				.transactionEnable()
				.fileDeleteAfterClose()
				.make();

		DB.TreeMapSink<IP, byte[]> sink = db
				.treeMap(Objects.requireNonNull(provider).name(), new IPSerializer(), Serializer.BYTE_ARRAY)
				.createFromSink();
		this.sink = sink;
	}

	public void dontExpectMore() {
		this.expectingMore = false;
	}

	public int reimainingLines() {
		return this.source.size();
	}

	public int processedLines() {
		return this.processedLines;
	}

	@Override
	public void run() {
		while (this.expectingMore || this.source.size() > 0) {
			SerializedLine beingProcessed = null;
			ArrayList<SerializedLine> availableForAdding = new ArrayList(this.source.size());
			try {
				Queues.drain(source, availableForAdding, this.source.size(), 1, TimeUnit.NANOSECONDS);
				for (SerializedLine currentLine : availableForAdding) {
					try {
						beingProcessed = currentLine;
						sink.put(currentLine.getStartIp(), currentLine.getInfo());
						this.processedLines++;
						this.listener.lineAdded(this.provider, currentLine);

					} catch (DBException.NotSorted e) {
						this.listener.lineOutOfOrder(this.provider, beingProcessed, e);
					}
				}
			} catch (InterruptedException e) {
				this.listener.builderInterrupted(this.provider, e);
			}
		}

	}

	public IPDatabase build() {
		return new IPDatabase(this.sink.create());
	}
}