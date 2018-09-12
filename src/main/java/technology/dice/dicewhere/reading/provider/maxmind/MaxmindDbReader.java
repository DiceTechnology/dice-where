package technology.dice.dicewhere.reading.provider.maxmind;


import technology.dice.dicewhere.parsing.LineParser;
import technology.dice.dicewhere.parsing.provider.DatabaseProvider;
import technology.dice.dicewhere.parsing.provider.maxmind.MaxmindLineParser;
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
		this.ipV4CSVPath = ipV4CSV;
		this.ipV6CSVPath = ipV6CSV;
		MaxmindLocationsParser locationsParser = new MaxmindLocationsParser();
		Map<String, MaxmindLocation> locations = locationsParser.locations(this.bufferedReaderforPath(locationNames, BUFFER_SIZE));
		this.parser = new MaxmindLineParser(locations);
	}

	@Override
	public LineParser parser() {
		return this.parser;
	}

	@Override
	protected Stream<String> lines() throws IOException {
		BufferedReader ipV4ChannelBufferedReader = this.bufferedReaderforPath(this.ipV4CSVPath, BUFFER_SIZE);
		BufferedReader ipV6ChannelBufferedReader = this.bufferedReaderforPath(this.ipV6CSVPath, BUFFER_SIZE);

		return Stream.concat(
				ipV4ChannelBufferedReader.lines().skip(1),
				ipV6ChannelBufferedReader.lines().skip(1)
		);
	}

	@Override
	public DatabaseProvider provider() {
		return DatabaseProvider.MAXMIND;
	}
}
