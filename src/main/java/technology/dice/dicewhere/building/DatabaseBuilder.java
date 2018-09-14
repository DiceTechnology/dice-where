package technology.dice.dicewhere.building;

import com.google.common.collect.Queues;
import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;
import org.mapdb.DB;
import org.mapdb.DBException;
import org.mapdb.DBMaker;
import org.mapdb.Serializer;
import technology.dice.dicewhere.api.api.IP;
import technology.dice.dicewhere.lineprocessing.SerializedLine;
import technology.dice.dicewhere.lineprocessing.serializers.IPSerializer;
import technology.dice.dicewhere.parsing.provider.DatabaseProvider;

public class DatabaseBuilder implements Runnable {
  private final ArrayBlockingQueue<SerializedLine> source;
  private final DatabaseBuilderListener listener;
  private final DatabaseProvider provider;
  private boolean expectingMore;
  private final DB.TreeMapSink<IP, byte[]> sink;
  private int processedLines = 0;

  public DatabaseBuilder(
      DatabaseProvider provider,
      ArrayBlockingQueue<SerializedLine> source,
      DatabaseBuilderListener listener) {

    this.source = source;
    this.expectingMore = true;
    this.listener = listener;
    this.provider = provider;
    DB db =
        DBMaker.tempFileDB()
            .checksumHeaderBypass()
            .fileLockDisable()
            .fileMmapEnable()
            .fileChannelEnable()
            .transactionEnable()
            .fileDeleteAfterClose()
            .make();

    DB.TreeMapSink<IP, byte[]> sink =
        db.treeMap(
                Objects.requireNonNull(provider).name(), new IPSerializer(), Serializer.BYTE_ARRAY)
            .createFromSink();
    this.sink = sink;
  }

  public void dontExpectMore() {
    expectingMore = false;
  }

  public int reimainingLines() {
    return source.size();
  }

  public int processedLines() {
    return processedLines;
  }

  @Override
  public void run() {
    while (expectingMore || source.size() > 0) {
      SerializedLine beingProcessed = null;
      ArrayList<SerializedLine> availableForAdding = new ArrayList(source.size());
      try {
        Queues.drain(source, availableForAdding, source.size(), 1, TimeUnit.NANOSECONDS);
        for (SerializedLine currentLine : availableForAdding) {
          try {
            beingProcessed = currentLine;
            sink.put(currentLine.getStartIp(), currentLine.getInfo());
            processedLines++;
            listener.lineAdded(provider, currentLine);

          } catch (DBException.NotSorted e) {
            listener.lineOutOfOrder(provider, beingProcessed, e);
          }
        }
      } catch (InterruptedException e) {
        listener.builderInterrupted(provider, e);
      }
    }
  }

  public IPDatabase build() {
    return new IPDatabase(sink.create());
  }
}
