/*
 * Copyright (C) 2018 - present by Dice Technology Ltd.
 *
 * Please see distribution for license.
 */

package technology.dice.dicewhere.reading;

import com.google.common.collect.Iterators;
import com.google.common.collect.Streams;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.io.SequenceInputStream;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipFile;
import org.jetbrains.annotations.NotNull;
import technology.dice.dicewhere.building.navigablemap.MapDbDatabaseBuilder;
import technology.dice.dicewhere.building.DatabaseBuilderListener;
import technology.dice.dicewhere.building.IPDatabase;
import technology.dice.dicewhere.building.navigablemap.NavigableMapIpDatabase;
import technology.dice.dicewhere.lineprocessing.LineProcessor;
import technology.dice.dicewhere.lineprocessing.LineProcessorListener;
import technology.dice.dicewhere.lineprocessing.LineprocessorListenerForProvider;
import technology.dice.dicewhere.lineprocessing.SerializedLine;
import technology.dice.dicewhere.parsing.LineParser;
import technology.dice.dicewhere.provider.ProviderKey;

/**
 * Base class providing data transformation between external IP data format provided by {@link
 * #lines()} method, returning standardised {@link IPDatabase}
 */
public abstract class CSVLineReader implements SourceReader {
  private static final int LINES_BUFFER = 100000;
  private final MapDbDatabaseBuilder.StorageMode storageMode;
  public static byte[] MAGIC_ZIP = {'P', 'K', 0x3, 0x4};
  public static int MAGIG_GZIP = 0xff00;

  public CSVLineReader() {
    this(MapDbDatabaseBuilder.StorageMode.FILE);
  }

  public CSVLineReader(@NotNull MapDbDatabaseBuilder.StorageMode storageMode) {
    this.storageMode = storageMode;
  }

  public abstract ProviderKey provider();

  public abstract LineParser parser();

  protected abstract Stream<String> lines() throws IOException;

  private static boolean isZipFile(Path path) {

    byte[] buffer = new byte[MAGIC_ZIP.length];
    try {
      RandomAccessFile raf = new RandomAccessFile(path.toFile(), "r");
      raf.readFully(buffer);
      for (int i = 0; i < MAGIC_ZIP.length; i++) {
        if (buffer[i] != MAGIC_ZIP[i]) {
          return false;
        }
      }
      raf.close();
    } catch (IOException e) {
      return false;
    }
    return true;
  }

  private static boolean isGZipped(Path path) {
    int magic = 0;
    try (RandomAccessFile raf = new RandomAccessFile(path.toFile(), "r")) {
      magic = raf.read() & 0xff | ((raf.read() << 8) & MAGIG_GZIP);
    } catch (Throwable e) {
      return false;
    }
    return magic == GZIPInputStream.GZIP_MAGIC;
  }

  public static BufferedReader bufferedReaderForPath(Path path, int bufferSize) throws IOException {
    BufferedReader br;
    if (isZipFile(path)) {

      ZipFile zipFile = new ZipFile(path.toFile());
      Enumeration<InputStream> zipEntries =
          Collections.enumeration(
              Streams.stream(Iterators.forEnumeration(zipFile.entries()))
                  .map(
                      ze -> {
                        try {
                          return zipFile.getInputStream(ze);
                        } catch (IOException e) {
                          throw new IllegalArgumentException(e);
                        }
                      })
                  .collect(Collectors.toCollection(ArrayList::new)));

      SequenceInputStream sequenceInputStream = new SequenceInputStream(zipEntries);
      br = new BufferedReader(new InputStreamReader(sequenceInputStream, StandardCharsets.UTF_8));
    } else if (isGZipped(path)) {
      InputStream is = new GZIPInputStream(new FileInputStream(path.toFile()));
      br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
    } else {
      FileChannel channel = FileChannel.open(path, StandardOpenOption.READ);
      br = new BufferedReader(Channels.newReader(channel, "UTF-8"), bufferSize);
    }
    return br;
  }

  public final NavigableMapIpDatabase read(
      boolean retainOriginalLine,
      LineReaderListener readerListener,
      LineProcessorListener processListener,
      DatabaseBuilderListener buildingListener,
      int workersCount) {

    long before = System.currentTimeMillis();
    ExecutorService parserExecutorService =
        Executors.newCachedThreadPool(
            new ThreadFactoryBuilder().setNameFormat("parser-%d").build());
    ExecutorService setupExecutorService =
        Executors.newFixedThreadPool(
            2, new ThreadFactoryBuilder().setNameFormat("line-reader-setup-%d").build());

    try {
      BlockingQueue<SerializedLine> serializedLinesBuffer = new ArrayBlockingQueue<>(LINES_BUFFER);

      LineProcessor processor =
          new LineProcessor(
              parserExecutorService,
              serializedLinesBuffer,
              parser(),
              retainOriginalLine,
              new LineprocessorListenerForProvider(provider(), processListener),
              workersCount);

      MapDbDatabaseBuilder databaseBuilder =
          parser()
              .getDecorator()
              .map(
                  d ->
                      new MapDbDatabaseBuilder(
                          storageMode, provider(), serializedLinesBuffer, buildingListener, d))
              .orElseGet(
                  () ->
                      new MapDbDatabaseBuilder(
                          storageMode, provider(), serializedLinesBuffer, buildingListener));

      Future processorFuture = setupExecutorService.submit(processor);
      Future databaseBuilderFuture = setupExecutorService.submit(databaseBuilder);

      publishLinesToProcessor(readerListener, before, processor);

      // WARNING!! DO NOT CHANGE ORDER "dontExpectMore" is expecting `processorFuture` to be done
      processor.markDataComplete();
      processorFuture.get();
      databaseBuilder.dontExpectMore();
      databaseBuilderFuture.get();

      parserExecutorService.shutdown();
      parserExecutorService.awaitTermination(1, TimeUnit.HOURS);
      setupExecutorService.shutdown();

      readerListener.finished(
          provider(), databaseBuilder.processedLines(), System.currentTimeMillis() - before);
      return databaseBuilder.build();
    } catch (Exception e) {
      throw new RuntimeException("Line reader read failed", e);
    } finally {
      parserExecutorService.shutdown();
      setupExecutorService.shutdown();
    }
  }

  private void publishLinesToProcessor(
      LineReaderListener readerListener, long before, LineProcessor processor) throws IOException {
    final long[] n = {0};
    try (Stream<String> lines = lines()) {
      lines.forEach(
          line -> {
            n[0]++;
            processor.addLine(new RawLine(line, n[0]));
            readerListener.lineRead(
                provider(), new RawLine(line, n[0]), System.currentTimeMillis() - before);
          });
    }
  }
}
