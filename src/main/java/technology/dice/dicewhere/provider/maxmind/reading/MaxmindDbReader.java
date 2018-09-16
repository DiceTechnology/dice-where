/*
 * Copyright (C) 2018 - present by Dice Technology Ltd.
 *
 * Please see distribution for license.
 */

package technology.dice.dicewhere.provider.maxmind.reading;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;
import technology.dice.dicewhere.parsing.LineParser;
import technology.dice.dicewhere.provider.ProviderKey;
import technology.dice.dicewhere.provider.maxmind.MaxmindProviderKey;
import technology.dice.dicewhere.provider.maxmind.parsing.MaxmindLineParser;
import technology.dice.dicewhere.reading.LineReader;

public class MaxmindDbReader extends LineReader {
  private static final int BUFFER_SIZE = 1024 * 1024;
  private final MaxmindLineParser parser;
  private final Path ipV4CSVPath;
  private final Path ipV6CSVPath;
  private final Path anonymousCSVPath;

  public MaxmindDbReader(Path locationNames, Path ipV4CSV, Path ipV6CSV, Path anonymousCSV)
      throws IOException {

    ipV4CSVPath = ipV4CSV;
    ipV6CSVPath = ipV6CSV;
    anonymousCSVPath = anonymousCSV;
    MaxmindLocationsParser locationsParser = new MaxmindLocationsParser();
    Map<String, MaxmindLocation> locations =
        locationsParser.locations(bufferedReaderForPath(locationNames, BUFFER_SIZE));

    if (getAnonymousCSVPath().isPresent()) {
      parser =
          new MaxmindLineParser(
              locations,
              new MaxmindAnonymousParser(bufferedReaderForPath(anonymousCSVPath, BUFFER_SIZE)));
    } else {
      parser = new MaxmindLineParser(locations);
    }
  }

  public MaxmindDbReader(Path locationNames, Path ipV4CSV, Path ipV6CSV) throws IOException {
    this(locationNames, ipV4CSV, ipV6CSV, null);
  }

  @Override
  public LineParser parser() {
    return parser;
  }

  @Override
  protected Stream<String> lines() throws IOException {
    BufferedReader ipV4ChannelBufferedReader = bufferedReaderForPath(ipV4CSVPath, BUFFER_SIZE);
    BufferedReader ipV6ChannelBufferedReader = bufferedReaderForPath(ipV6CSVPath, BUFFER_SIZE);

    return Stream.concat(
        ipV4ChannelBufferedReader.lines().skip(1), ipV6ChannelBufferedReader.lines().skip(1));
  }

  protected Optional<Path> getAnonymousCSVPath() {
    return Optional.ofNullable(anonymousCSVPath);
  }

  @Override
  public ProviderKey provider() {
    return MaxmindProviderKey.of();
  }
}
