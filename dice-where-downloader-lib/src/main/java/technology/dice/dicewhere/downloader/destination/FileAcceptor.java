package technology.dice.dicewhere.downloader.destination;

import java.net.URI;
import java.time.Instant;
import java.util.Optional;
import technology.dice.dicewhere.downloader.md5.MD5Checksum;
import technology.dice.dicewhere.downloader.stream.StreamConsumer;

public interface FileAcceptor<T> {

  StreamConsumer<T> getStreamConsumer(
      MD5Checksum originalFileMd5, Instant originalFileTimestamp, boolean noMd5Check);

  boolean destinationExists();

  boolean destinationWritable();

  Optional<MD5Checksum> existingFileMd5();

  URI getUri();
}
