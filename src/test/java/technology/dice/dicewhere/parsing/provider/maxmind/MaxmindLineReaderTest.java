package technology.dice.dicewhere.parsing.provider.maxmind;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import technology.dice.dicewhere.building.DatabaseBuilderListener;
import technology.dice.dicewhere.building.IPDatabase;
import technology.dice.dicewhere.lineprocessing.LineProcessorListener;
import technology.dice.dicewhere.provider.maxmind.MaxmindProviderKey;
import technology.dice.dicewhere.reading.LineReaderListener;
import technology.dice.dicewhere.provider.maxmind.reading.MaxmindDbReader;

public class MaxmindLineReaderTest {
  @Test
  public void clean() throws IOException {
    Path v4 =
        Paths.get(
            MaxmindLineReaderTest.class
                .getClassLoader()
                .getResource("provider/maxmind/tinyValidV4.csv")
                .getFile());
    Path v6 =
        Paths.get(
            MaxmindLineReaderTest.class
                .getClassLoader()
                .getResource("provider/maxmind/tinyValidV6.csv")
                .getFile());
    Path locationNames =
        Paths.get(
            MaxmindLineReaderTest.class
                .getClassLoader()
                .getResource("provider/maxmind/GeoLite2-City-Locations-en.csv.zip")
                .getFile());
    LineReaderListener readerListener = Mockito.mock(LineReaderListener.class);
    LineProcessorListener processorListener = Mockito.mock(LineProcessorListener.class);
    DatabaseBuilderListener builderListener = Mockito.mock(DatabaseBuilderListener.class);
    MaxmindDbReader dbIpReader = new MaxmindDbReader(locationNames, v4, v6);
    IPDatabase database =
        dbIpReader.read(false, readerListener, processorListener, builderListener);
    long dbSize = database.size();
    Assert.assertEquals(18, dbSize);
    Mockito.verify(readerListener, Mockito.times(1))
        .finished(Mockito.eq(MaxmindProviderKey.of()), Mockito.eq(dbSize), Mockito.anyLong());
    Mockito.verify(readerListener, Mockito.times((int) dbSize))
        .lineRead(Mockito.eq(MaxmindProviderKey.of()), Mockito.any(), Mockito.anyLong());
    Mockito.verify(processorListener, Mockito.times(1))
        .finished(Mockito.eq(MaxmindProviderKey.of()), Mockito.eq(dbSize), Mockito.anyLong());
    Mockito.verify(processorListener, Mockito.times((int) dbSize))
        .lineProcessed(Mockito.eq(MaxmindProviderKey.of()), Mockito.any(), Mockito.anyLong());
    Mockito.verify(processorListener, Mockito.times((int) dbSize))
        .lineParsed(Mockito.eq(MaxmindProviderKey.of()), Mockito.any(), Mockito.anyLong());
    Mockito.verify(processorListener, Mockito.never())
        .dequeueError(Mockito.any(), Mockito.any(), Mockito.any());
    Mockito.verify(processorListener, Mockito.never())
        .serializeError(Mockito.any(), Mockito.any(), Mockito.any());
    Mockito.verify(processorListener, Mockito.never())
        .processorInterrupted(Mockito.any(), Mockito.any());
    Mockito.verify(processorListener, Mockito.never())
        .enqueueError(Mockito.any(), Mockito.any(), Mockito.any());
    Mockito.verify(processorListener, Mockito.never())
        .parseError(Mockito.any(), Mockito.any(), Mockito.any());
    Mockito.verify(processorListener, Mockito.never())
        .parseError(Mockito.any(), Mockito.any(), Mockito.any());
    Mockito.verify(builderListener, Mockito.never())
        .lineOutOfOrder(Mockito.any(), Mockito.any(), Mockito.any());
    Mockito.verify(builderListener, Mockito.never())
        .builderInterrupted(Mockito.any(), Mockito.any());
    Mockito.verify(builderListener, Mockito.times((int) dbSize))
        .lineAdded(Mockito.eq(MaxmindProviderKey.of()), Mockito.any());
  }

  @Test
  public void invalidLines() throws IOException {
    Path v4 =
        Paths.get(
            MaxmindLineReaderTest.class
                .getClassLoader()
                .getResource("provider/maxmind/tinyInvalidLinesV4.csv")
                .getFile());
    Path v6 =
        Paths.get(
            MaxmindLineReaderTest.class
                .getClassLoader()
                .getResource("provider/maxmind/tinyInvalidLinesV6.csv")
                .getFile());
    Path locationNames =
        Paths.get(
            MaxmindLineReaderTest.class
                .getClassLoader()
                .getResource("provider/maxmind/GeoLite2-City-Locations-en.csv.zip")
                .getFile());
    LineReaderListener readerListener = Mockito.mock(LineReaderListener.class);
    LineProcessorListener processorListener = Mockito.mock(LineProcessorListener.class);
    DatabaseBuilderListener builderListener = Mockito.mock(DatabaseBuilderListener.class);
    MaxmindDbReader dbIpReader = new MaxmindDbReader(locationNames, v4, v6);
    IPDatabase database =
        dbIpReader.read(false, readerListener, processorListener, builderListener);
    long dbSize = database.size();
    Assert.assertEquals(11, dbSize);
    Mockito.verify(readerListener, Mockito.times(1))
        .finished(Mockito.eq(MaxmindProviderKey.of()), Mockito.eq(dbSize), Mockito.anyLong());
    Mockito.verify(readerListener, Mockito.times(18))
        .lineRead(Mockito.eq(MaxmindProviderKey.of()), Mockito.any(), Mockito.anyLong());
    Mockito.verify(processorListener, Mockito.times(1))
        .finished(Mockito.eq(MaxmindProviderKey.of()), Mockito.eq(dbSize), Mockito.anyLong());
    Mockito.verify(processorListener, Mockito.times(11))
        .lineProcessed(Mockito.eq(MaxmindProviderKey.of()), Mockito.any(), Mockito.anyLong());
    Mockito.verify(processorListener, Mockito.times(11))
        .lineParsed(Mockito.eq(MaxmindProviderKey.of()), Mockito.any(), Mockito.anyLong());
    Mockito.verify(processorListener, Mockito.never())
        .dequeueError(Mockito.any(), Mockito.any(), Mockito.any());
    Mockito.verify(processorListener, Mockito.never())
        .serializeError(Mockito.any(), Mockito.any(), Mockito.any());
    Mockito.verify(processorListener, Mockito.never())
        .processorInterrupted(Mockito.any(), Mockito.any());
    Mockito.verify(processorListener, Mockito.never())
        .enqueueError(Mockito.any(), Mockito.any(), Mockito.any());
    Mockito.verify(processorListener, Mockito.times(7))
        .parseError(Mockito.eq(MaxmindProviderKey.of()), Mockito.any(), Mockito.any());
    Mockito.verify(builderListener, Mockito.never())
        .lineOutOfOrder(Mockito.any(), Mockito.any(), Mockito.any());
    Mockito.verify(builderListener, Mockito.never())
        .builderInterrupted(Mockito.any(), Mockito.any());
    Mockito.verify(builderListener, Mockito.times((int) dbSize))
        .lineAdded(Mockito.eq(MaxmindProviderKey.of()), Mockito.any());
  }

  @Test
  public void outOfOrder() throws IOException {
    Path v4 =
        Paths.get(
            MaxmindLineReaderTest.class
                .getClassLoader()
                .getResource("provider/maxmind/tinyNotSortedV4.csv")
                .getFile());
    Path v6 =
        Paths.get(
            MaxmindLineReaderTest.class
                .getClassLoader()
                .getResource("provider/maxmind/tinyNotSortedV6.csv")
                .getFile());
    Path locationNames =
        Paths.get(
            MaxmindLineReaderTest.class
                .getClassLoader()
                .getResource("provider/maxmind/GeoLite2-City-Locations-en.csv.zip")
                .getFile());
    LineReaderListener readerListener = Mockito.mock(LineReaderListener.class);
    LineProcessorListener processorListener = Mockito.mock(LineProcessorListener.class);
    DatabaseBuilderListener builderListener = Mockito.mock(DatabaseBuilderListener.class);
    MaxmindDbReader dbIpReader = new MaxmindDbReader(locationNames, v4, v6);
    IPDatabase database =
        dbIpReader.read(false, readerListener, processorListener, builderListener);
    long dbSize = database.size();
    Assert.assertEquals(9, dbSize);
    Mockito.verify(readerListener, Mockito.times(1))
        .finished(Mockito.eq(MaxmindProviderKey.of()), Mockito.eq(dbSize), Mockito.anyLong());
    Mockito.verify(readerListener, Mockito.times(11))
        .lineRead(Mockito.eq(MaxmindProviderKey.of()), Mockito.any(), Mockito.anyLong());
    Mockito.verify(processorListener, Mockito.times(1))
        .finished(Mockito.eq(MaxmindProviderKey.of()), Mockito.eq(11L), Mockito.anyLong());
    Mockito.verify(processorListener, Mockito.times(11))
        .lineProcessed(Mockito.eq(MaxmindProviderKey.of()), Mockito.any(), Mockito.anyLong());
    Mockito.verify(processorListener, Mockito.times(11))
        .lineParsed(Mockito.eq(MaxmindProviderKey.of()), Mockito.any(), Mockito.anyLong());
    Mockito.verify(processorListener, Mockito.never())
        .dequeueError(Mockito.any(), Mockito.any(), Mockito.any());
    Mockito.verify(processorListener, Mockito.never())
        .serializeError(Mockito.any(), Mockito.any(), Mockito.any());
    Mockito.verify(processorListener, Mockito.never())
        .processorInterrupted(Mockito.any(), Mockito.any());
    Mockito.verify(processorListener, Mockito.never())
        .enqueueError(Mockito.any(), Mockito.any(), Mockito.any());
    Mockito.verify(processorListener, Mockito.never())
        .parseError(Mockito.any(), Mockito.any(), Mockito.any());
    Mockito.verify(builderListener, Mockito.times(2))
        .lineOutOfOrder(Mockito.eq(MaxmindProviderKey.of()), Mockito.any(), Mockito.any());
    Mockito.verify(builderListener, Mockito.never())
        .builderInterrupted(Mockito.any(), Mockito.any());
    Mockito.verify(builderListener, Mockito.times((int) dbSize))
        .lineAdded(Mockito.eq(MaxmindProviderKey.of()), Mockito.any());
  }
}
