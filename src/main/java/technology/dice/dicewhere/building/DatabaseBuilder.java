/*
 * Copyright (C) 2018 - present by Dice Technology Ltd.
 *
 * Please see distribution for license.
 */

package technology.dice.dicewhere.building;

import com.google.common.collect.Queues;
import com.google.protobuf.ByteString;
import org.jetbrains.annotations.NotNull;
import org.mapdb.DB;
import org.mapdb.DBException;
import org.mapdb.DBMaker;
import org.mapdb.Serializer;
import technology.dice.dicewhere.api.api.IP;
import technology.dice.dicewhere.api.api.IpInformation;
import technology.dice.dicewhere.decorator.Decorator;
import technology.dice.dicewhere.decorator.DecoratorInformation;
import technology.dice.dicewhere.lineprocessing.SerializedLine;
import technology.dice.dicewhere.lineprocessing.serializers.IPSerializer;
import technology.dice.dicewhere.lineprocessing.serializers.protobuf.IPInformationProto;
import technology.dice.dicewhere.provider.ProviderKey;
import technology.dice.dicewhere.utils.ProtoValueConverter;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

public class DatabaseBuilder implements Runnable {
  private final BlockingQueue<SerializedLine> source;
  private final DatabaseBuilderListener listener;
  private final ProviderKey provider;
  private final DB.TreeMapSink<IP, byte[]> sink;
  private boolean expectingMore;
  private int processedLines = 0;
  private final Decorator<? extends DecoratorInformation> decorator;

  public DatabaseBuilder(
      ProviderKey provider,
      BlockingQueue<SerializedLine> source,
      DatabaseBuilderListener listener,
      Decorator<? extends DecoratorInformation> decorator) {
    this(StorageMode.FILE, provider, source, listener, decorator);
  }

  public DatabaseBuilder(
      StorageMode storageMode,
      ProviderKey provider,
      BlockingQueue<SerializedLine> source,
      DatabaseBuilderListener listener) {

    this(storageMode, provider, source, listener, null);
  }

  public DatabaseBuilder(
      StorageMode storageMode,
      ProviderKey provider,
      BlockingQueue<SerializedLine> source,
      DatabaseBuilderListener listener,
      Decorator<? extends DecoratorInformation> decorator) {
    this.source = source;
    this.expectingMore = true;
    this.listener = listener;
    this.provider = provider;
    DB db = createDB(storageMode);

    DB.TreeMapSink<IP, byte[]> sink =
        db.treeMap(
                Objects.requireNonNull(provider).name(), new IPSerializer(), Serializer.BYTE_ARRAY)
            .createFromSink();
    this.sink = sink;
    this.decorator = decorator;
  }

  @NotNull
  private DB createDB(StorageMode storageMode) {
    DB db;
    switch (storageMode) {
      case HEAP:
        db = DBMaker.heapDB().checksumHeaderBypass().make();
        break;
      case HEAP_BYTE_ARRAY:
        db = DBMaker.memoryDB().checksumHeaderBypass().make();
        break;
      case OFF_HEAP:
        db = DBMaker.memoryDirectDB().checksumHeaderBypass().make();
        break;
      case FILE:
      default:
        db =
            DBMaker.tempFileDB()
                .checksumHeaderBypass()
                .fileLockDisable()
                .fileMmapEnable()
                .fileChannelEnable()
                .transactionEnable()
                .fileDeleteAfterClose()
                .make();
        break;
    }
    return db;
  }

  public void dontExpectMore() {
    expectingMore = false;
  }

  public int remainingLines() {
    return source.size();
  }

  public int processedLines() {
    return processedLines;
  }

  protected Optional<Decorator<? extends DecoratorInformation>> getDecorator() {
    return Optional.ofNullable(decorator);
  }

  @Override
  public void run() {
    while (expectingMore || source.size() > 0) {
      SerializedLine beingProcessed = null;
      List<SerializedLine> availableForAdding = new ArrayList<>(source.size());
      try {
        Queues.drain(source, availableForAdding, source.size(), 1, TimeUnit.NANOSECONDS);
        for (SerializedLine currentLine : availableForAdding) {
          try {
            beingProcessed = currentLine;
            decorateEntry(currentLine.getParsedLine().getInfo())
                .forEach(i -> sink.put(i.getStartOfRange(), buildIpProtobuf(i).toByteArray()));
            processedLines++;
            listener.lineAdded(provider, currentLine);

          } catch (DBException.NotSorted e) {
            listener.lineOutOfOrder(provider, beingProcessed, e);
          } catch (Exception e) {
            throw new RuntimeException("Database builder interrupted", e);
          }
        }
      } catch (InterruptedException e) {
        listener.builderInterrupted(provider, e);
        throw new RuntimeException("Database builder interrupted", e);
      }
    }
  }

  private Stream<IpInformation> decorateEntry(IpInformation entry) throws UnknownHostException {
    if (getDecorator().isPresent()) {
      return getDecorator().get().decorate(entry);
    } else {
      return Stream.of(entry);
    }
  }

  private IPInformationProto.IpInformationProto buildIpProtobuf(IpInformation input) {
    IPInformationProto.IpInformationProto.Builder messageBuilder =
        IPInformationProto.IpInformationProto.newBuilder()
            .setCity(input.getCity().orElse(""))
            .setGeonameId(input.getGeonameId().orElse(""))
            .setCountryCodeAlpha2(input.getCountryCodeAlpha2())
            .setLeastSpecificDivision(input.getLeastSpecificDivision().orElse(""))
            .setMostSpecificDivision(input.getMostSpecificDivision().orElse(""))
            .setPostcode(input.getPostcode().orElse(""))
            .setStartOfRange(ByteString.copyFrom(input.getStartOfRange().getBytes()))
            .setEndOfRange(ByteString.copyFrom(input.getEndOfRange().getBytes()))
            .setIsVpn(ProtoValueConverter.toThreeStateValue(input.isVpn().orElse(null)));

    input.getOriginalLine().ifPresent(messageBuilder::setOriginalLine);

    return messageBuilder.build();
  }

  public IPDatabase build() {
    return new IPDatabase(sink.create());
  }

  public enum StorageMode {
    HEAP,
    HEAP_BYTE_ARRAY,
    OFF_HEAP,
    FILE
  }
}
