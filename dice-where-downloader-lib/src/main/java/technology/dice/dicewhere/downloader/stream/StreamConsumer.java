package technology.dice.dicewhere.downloader.stream;

import java.io.IOException;
import java.io.InputStream;

@FunctionalInterface
public interface StreamConsumer<T> {
  T consume(InputStream stream, long size) throws IOException;
}
