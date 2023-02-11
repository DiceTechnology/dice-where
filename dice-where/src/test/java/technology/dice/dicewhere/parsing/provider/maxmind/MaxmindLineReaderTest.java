/*
 * Copyright (C) 2018 - present by Dice Technology Ltd.
 *
 * Please see distribution for license.
 */

package technology.dice.dicewhere.parsing.provider.maxmind;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.jupiter.api.Test;
import technology.dice.dicewhere.building.DatabaseBuilderListener;
import technology.dice.dicewhere.building.navigablemap.NavigableMapIpDatabase;
import technology.dice.dicewhere.lineprocessing.LineProcessorListener;
import technology.dice.dicewhere.provider.maxmind.MaxmindProviderKey;
import technology.dice.dicewhere.provider.maxmind.reading.MaxmindDbReader;
import technology.dice.dicewhere.reading.LineReaderListener;

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

    NavigableMapIpDatabase database = dbIpReader.read(false, readerListener, processorListener, builderListener, 4);
    long dbSize = database.size();

    assertEquals(18, dbSize);
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

    NavigableMapIpDatabase database = dbIpReader.read(false, readerListener, processorListener, builderListener, 4);
    long dbSize = database.size();

    assertEquals(11, dbSize);
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

    NavigableMapIpDatabase database = dbIpReader.read(false, readerListener, processorListener, builderListener, 4);
    long dbSize = database.size();

    assertEquals(9, dbSize);
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