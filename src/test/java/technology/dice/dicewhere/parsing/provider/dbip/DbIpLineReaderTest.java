/*
 * Copyright (C) 2018 - present by Dice Technology Ltd.
 *
 * Please see distribution for license.
 */

package technology.dice.dicewhere.parsing.provider.dbip;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import technology.dice.dicewhere.building.DatabaseBuilderListener;
import technology.dice.dicewhere.building.IPDatabase;
import technology.dice.dicewhere.lineprocessing.LineProcessorListener;
import technology.dice.dicewhere.provider.dbip.DbIpProviderKey;
import technology.dice.dicewhere.provider.dbip.reading.DbIpLineReader;
import technology.dice.dicewhere.provider.dbip.reading.DbIpLocarionAndIspLineReader;
import technology.dice.dicewhere.reading.LineReaderListener;

public class DbIpLineReaderTest {
  @Test
  public void clean() throws IOException {
    Path path =
        Paths.get(
            DbIpLineReaderTest.class
                .getClassLoader()
                .getResource("provider/dbip/tinyValid.csv")
                .getFile());
    LineReaderListener readerListener = Mockito.mock(LineReaderListener.class);
    LineProcessorListener processorListener = Mockito.mock(LineProcessorListener.class);
    DatabaseBuilderListener builderListener = Mockito.mock(DatabaseBuilderListener.class);
    DbIpLineReader dbIpReader = new DbIpLocarionAndIspLineReader(path);
    IPDatabase database =
        dbIpReader.read(false, readerListener, processorListener, builderListener, 4);
    long dbSize = database.size();
    Assert.assertEquals(22, dbSize);
    Mockito.verify(readerListener, Mockito.times(1))
        .finished(Mockito.eq(DbIpProviderKey.of()), Mockito.eq(dbSize), Mockito.anyLong());
    Mockito.verify(readerListener, Mockito.times((int) dbSize))
        .lineRead(Mockito.eq(DbIpProviderKey.of()), Mockito.any(), Mockito.anyLong());
    Mockito.verify(processorListener, Mockito.times(1))
        .finished(Mockito.eq(DbIpProviderKey.of()), Mockito.eq(dbSize), Mockito.anyLong());
    Mockito.verify(processorListener, Mockito.times((int) dbSize))
        .lineProcessed(Mockito.eq(DbIpProviderKey.of()), Mockito.any(), Mockito.anyLong());
    Mockito.verify(processorListener, Mockito.times((int) dbSize))
        .lineParsed(Mockito.eq(DbIpProviderKey.of()), Mockito.any(), Mockito.anyLong());
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
    Mockito.verify(builderListener, Mockito.never())
        .lineOutOfOrder(Mockito.any(), Mockito.any(), Mockito.any());
    Mockito.verify(builderListener, Mockito.never())
        .builderInterrupted(Mockito.any(), Mockito.any());
    Mockito.verify(builderListener, Mockito.times((int) dbSize))
        .lineAdded(Mockito.eq(DbIpProviderKey.of()), Mockito.any());
  }

  @Test
  public void invalidLines() throws IOException {
    Path path =
        Paths.get(
            DbIpLineReaderTest.class
                .getClassLoader()
                .getResource("provider/dbip/tinyInvalidLines.csv")
                .getFile());
    LineReaderListener readerListener = Mockito.mock(LineReaderListener.class);
    LineProcessorListener processorListener = Mockito.mock(LineProcessorListener.class);
    DatabaseBuilderListener builderListener = Mockito.mock(DatabaseBuilderListener.class);
    DbIpLineReader dbIpReader = new DbIpLocarionAndIspLineReader(path);
    IPDatabase database =
        dbIpReader.read(false, readerListener, processorListener, builderListener, 4);
    long dbSize = database.size();
    Assert.assertEquals(16, dbSize);
    Mockito.verify(readerListener, Mockito.times(1))
        .finished(Mockito.eq(DbIpProviderKey.of()), Mockito.eq(dbSize), Mockito.anyLong());
    Mockito.verify(readerListener, Mockito.times(20))
        .lineRead(Mockito.eq(DbIpProviderKey.of()), Mockito.any(), Mockito.anyLong());
    Mockito.verify(processorListener, Mockito.times(1))
        .finished(Mockito.eq(DbIpProviderKey.of()), Mockito.eq(dbSize), Mockito.anyLong());
    Mockito.verify(processorListener, Mockito.times(16))
        .lineProcessed(Mockito.eq(DbIpProviderKey.of()), Mockito.any(), Mockito.anyLong());
    Mockito.verify(processorListener, Mockito.times(16))
        .lineParsed(Mockito.eq(DbIpProviderKey.of()), Mockito.any(), Mockito.anyLong());
    Mockito.verify(processorListener, Mockito.never())
        .dequeueError(Mockito.any(), Mockito.any(), Mockito.any());
    Mockito.verify(processorListener, Mockito.never())
        .serializeError(Mockito.any(), Mockito.any(), Mockito.any());
    Mockito.verify(processorListener, Mockito.never())
        .processorInterrupted(Mockito.any(), Mockito.any());
    Mockito.verify(processorListener, Mockito.never())
        .enqueueError(Mockito.any(), Mockito.any(), Mockito.any());
    Mockito.verify(processorListener, Mockito.times(4))
        .parseError(Mockito.eq(DbIpProviderKey.of()), Mockito.any(), Mockito.any());
    Mockito.verify(builderListener, Mockito.never())
        .lineOutOfOrder(Mockito.any(), Mockito.any(), Mockito.any());
    Mockito.verify(builderListener, Mockito.never())
        .builderInterrupted(Mockito.any(), Mockito.any());
    Mockito.verify(builderListener, Mockito.times((int) dbSize))
        .lineAdded(Mockito.eq(DbIpProviderKey.of()), Mockito.any());
  }

  @Test
  public void outOfOrder() throws IOException {
    Path path =
        Paths.get(
            DbIpLineReaderTest.class
                .getClassLoader()
                .getResource("provider/dbip/tinyNotSorted.csv")
                .getFile());
    LineReaderListener readerListener = Mockito.mock(LineReaderListener.class);
    LineProcessorListener processorListener = Mockito.mock(LineProcessorListener.class);
    DatabaseBuilderListener builderListener = Mockito.mock(DatabaseBuilderListener.class);
    DbIpLineReader dbIpReader = new DbIpLocarionAndIspLineReader(path);
    IPDatabase database =
        dbIpReader.read(false, readerListener, processorListener, builderListener, 4);
    long dbSize = database.size();
    Assert.assertEquals(3, dbSize);
    Mockito.verify(readerListener, Mockito.times(1))
        .finished(Mockito.eq(DbIpProviderKey.of()), Mockito.eq(dbSize), Mockito.anyLong());
    Mockito.verify(readerListener, Mockito.times(4))
        .lineRead(Mockito.eq(DbIpProviderKey.of()), Mockito.any(), Mockito.anyLong());
    Mockito.verify(processorListener, Mockito.times(1))
        .finished(Mockito.eq(DbIpProviderKey.of()), Mockito.eq(4L), Mockito.anyLong());
    Mockito.verify(processorListener, Mockito.times(4))
        .lineProcessed(Mockito.eq(DbIpProviderKey.of()), Mockito.any(), Mockito.anyLong());
    Mockito.verify(processorListener, Mockito.times(4))
        .lineParsed(Mockito.eq(DbIpProviderKey.of()), Mockito.any(), Mockito.anyLong());
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
    Mockito.verify(builderListener, Mockito.times(1))
        .lineOutOfOrder(Mockito.eq(DbIpProviderKey.of()), Mockito.any(), Mockito.any());
    Mockito.verify(builderListener, Mockito.never())
        .builderInterrupted(Mockito.any(), Mockito.any());
    Mockito.verify(builderListener, Mockito.times((int) dbSize))
        .lineAdded(Mockito.eq(DbIpProviderKey.of()), Mockito.any());
  }
}
