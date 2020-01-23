package technology.dice.dicewhere.downloader.destination;

import java.net.URI;
import java.util.Optional;
import technology.dice.dicewhere.downloader.md5.MD5Checksum;
import technology.dice.dicewhere.downloader.stream.StreamConsumer;

public interface FileAcceptor<T, META> {
  StreamConsumer<T> getStreamConsumer(Optional<META> meta);

  default StreamConsumer<T> getStreamConsumer() {
    return getStreamConsumer(Optional.empty());
  }

  default StreamConsumer<T> getStreamConsumer(META meta) {
    return getStreamConsumer(Optional.of(meta));
  }

  boolean destinationExists();

  boolean destinationWritable();

  Optional<MD5Checksum> existingFileMd5();

  URI getUri();
}
