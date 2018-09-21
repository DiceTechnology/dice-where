/*
 * Copyright (C) 2018 - present by Dice Technology Ltd.
 *
 * Please see distribution for license.
 */

package technology.dice.dicewhere.parsing.provider.maxmind;

import org.junit.Assert;
import org.junit.Test;
import technology.dice.dicewhere.building.DatabaseBuilderListener;
import technology.dice.dicewhere.building.IPDatabase;
import technology.dice.dicewhere.lineprocessing.LineProcessorListener;
import technology.dice.dicewhere.provider.maxmind.MaxmindProviderKey;
import technology.dice.dicewhere.provider.maxmind.reading.MaxmindDbReader;
import technology.dice.dicewhere.reading.LineReaderListener;

import java.nio.file.Path;
import java.nio.file.Paths;

import static org.mockito.Mockito.*;

public class MaxmindLineReaderTest {
  private LineReaderListener readerListener = mock(LineReaderListener.class);
  private LineProcessorListener processorListener = mock(LineProcessorListener.class);
  private DatabaseBuilderListener builderListener = mock(DatabaseBuilderListener.class);

  @Test
  public void clean() throws Exception {
    Path v4 = getPath("provider/maxmind/tinyValidV4.csv");
    Path v6 = getPath("provider/maxmind/tinyValidV6.csv");
    Path locationNames = getPath("provider/maxmind/GeoLite2-City-Locations-en.csv.zip");
    MaxmindDbReader dbIpReader = new MaxmindDbReader(locationNames, v4, v6);

    IPDatabase database = dbIpReader.read(false, readerListener, processorListener, builderListener);
    long dbSize = database.size();

    Assert.assertEquals(18, dbSize);
    verify(readerListener, times((int) dbSize)).lineRead(eq(MaxmindProviderKey.of()), any(), anyLong());
    verify(builderListener, never()).lineOutOfOrder(any(), any(), any());
    verify(processorListener, never()).parseError(any(), any(), any());
    verifyProcessorListener(dbSize);
    verifyReaderListener(dbSize);
    verifyBuilderListener(dbSize);
  }

  @Test
  public void invalidLines() throws Exception {
    Path v4 = getPath("provider/maxmind/tinyInvalidLinesV4.csv");
    Path v6 = getPath("provider/maxmind/tinyInvalidLinesV6.csv");
    Path locationNames = getPath("provider/maxmind/GeoLite2-City-Locations-en.csv.zip");
    MaxmindDbReader dbIpReader = new MaxmindDbReader(locationNames, v4, v6);

    IPDatabase database = dbIpReader.read(false, readerListener, processorListener, builderListener);
    long dbSize = database.size();

    Assert.assertEquals(11, dbSize);
    verify(readerListener, times(18)).lineRead(eq(MaxmindProviderKey.of()), any(), anyLong());
    verify(processorListener, times(7)).parseError(eq(MaxmindProviderKey.of()), any(), any());
    verify(builderListener, never()).lineOutOfOrder(any(), any(), any());
    verifyProcessorListener(11);
    verifyReaderListener(dbSize);
    verifyBuilderListener(dbSize);
  }

  @Test
  public void outOfOrder() throws Exception {
    Path v4 = getPath("provider/maxmind/tinyNotSortedV4.csv");
    Path v6 = getPath("provider/maxmind/tinyNotSortedV6.csv");
    Path locationNames = getPath("provider/maxmind/GeoLite2-City-Locations-en.csv.zip");
    MaxmindDbReader dbIpReader = new MaxmindDbReader(locationNames, v4, v6);

    IPDatabase database = dbIpReader.read(false, readerListener, processorListener, builderListener);
    long dbSize = database.size();

    Assert.assertEquals(9, dbSize);
    verify(readerListener, times(11)).lineRead(eq(MaxmindProviderKey.of()), any(), anyLong());
    verify(builderListener, times(2)).lineOutOfOrder(eq(MaxmindProviderKey.of()), any(), any());
    verify(processorListener, never()).parseError(any(), any(), any());
    verifyProcessorListener(11);
    verifyReaderListener(dbSize);
    verifyBuilderListener(dbSize);
  }

  private Path getPath(String location) throws Exception {
    return Paths.get(MaxmindLineReaderTest.class.getClassLoader().getResource(location).getFile());
  }

  private void verifyReaderListener(long dbSize) {
    verify(readerListener, times(1)).finished(eq(MaxmindProviderKey.of()), eq(dbSize), anyLong());
  }

  private void verifyBuilderListener(long times) {
    verify(builderListener, times((int) times)).lineAdded(eq(MaxmindProviderKey.of()), any());
    verify(builderListener, never()).builderInterrupted(any(), any());
  }

  private void verifyProcessorListener(long times) {
    verify(processorListener, times(1)).finished(eq(MaxmindProviderKey.of()), eq(times), anyLong());
    verify(processorListener, times((int) times)).lineProcessed(eq(MaxmindProviderKey.of()), any(), anyLong());
    verify(processorListener, times((int) times)).lineParsed(eq(MaxmindProviderKey.of()), any(), anyLong());
    verify(processorListener, never()).dequeueError(any(), any(), any());
    verify(processorListener, never()).serializeError(any(), any(), any());
    verify(processorListener, never()).processorInterrupted(any(), any());
    verify(processorListener, never()).enqueueError(any(), any(), any());
  }
}