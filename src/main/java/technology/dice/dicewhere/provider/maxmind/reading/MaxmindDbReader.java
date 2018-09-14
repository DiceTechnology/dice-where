/*
 * Copyright (C) 2018 - present by Dice Technology Ltd.
 *
 * Please see distribution for license.
 */

package technology.dice.dicewhere.provider.maxmind.reading;

import technology.dice.dicewhere.parsing.LineParser;
import technology.dice.dicewhere.provider.ProviderKey;
import technology.dice.dicewhere.provider.maxmind.MaxmindProviderKey;
import technology.dice.dicewhere.provider.maxmind.parsing.MaxmindLineParser;
import technology.dice.dicewhere.reading.LineReader;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import java.util.stream.Stream;

public class MaxmindDbReader extends LineReader {
  private static final int BUFFER_SIZE = 1024 * 1024;
  private final MaxmindLineParser parser;
  private final Path ipV4CSVPath;
  private final Path ipV6CSVPath;

  public MaxmindDbReader(Path locationNames, Path ipV4CSV, Path ipV6CSV) throws IOException {
    ipV4CSVPath = ipV4CSV;
    ipV6CSVPath = ipV6CSV;
    MaxmindLocationsParser locationsParser = new MaxmindLocationsParser();
    Map<String, MaxmindLocation> locations =
        locationsParser.locations(bufferedReaderforPath(locationNames, BUFFER_SIZE));
    parser = new MaxmindLineParser(locations);
  }

  @Override
  public LineParser parser() {
    return parser;
  }

  @Override
  protected Stream<String> lines() throws IOException {
    BufferedReader ipV4ChannelBufferedReader = bufferedReaderforPath(ipV4CSVPath, BUFFER_SIZE);
    BufferedReader ipV6ChannelBufferedReader = bufferedReaderforPath(ipV6CSVPath, BUFFER_SIZE);

    return Stream.concat(
        ipV4ChannelBufferedReader.lines().skip(1), ipV6ChannelBufferedReader.lines().skip(1));
  }

  @Override
  public ProviderKey provider() {
    return new MaxmindProviderKey();
  }
}
