package technology.dice.dicewhere.downloader.destination.local;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import technology.dice.dicewhere.downloader.destination.FileAcceptor;
import technology.dice.dicewhere.downloader.md5.MD5Checksum;
import technology.dice.dicewhere.downloader.stream.StreamConsumer;
import technology.dice.dicewhere.downloader.stream.StreamWithMD5Decorator;

public class LocalFileAcceptor implements FileAcceptor<Void> {

  private static final Logger LOG = LoggerFactory.getLogger(LocalFileAcceptor.class);
  public static final int BUFFER = 8192;

  private final Path destination;

  public LocalFileAcceptor(Path destination) {
    this.destination = destination;
  }

  @Override
  public StreamConsumer<Void> getStreamConsumer(
      MD5Checksum originalFileMd5, Instant originalFileTimestamp, boolean noMd5Check) {
    return (stream, size) -> {
      try {
        Files.createDirectories(destination);
        LOG.debug("Destination directory created");
      } catch (FileAlreadyExistsException e) {
        LOG.debug("Destination directory already exists");
      }
      Files.copy(stream, destination, StandardCopyOption.REPLACE_EXISTING);
      if ((!noMd5Check) && (!originalFileMd5.matches(stream.md5()))) {
        LOG.error("MD5 mismatch. Deleting destination file");
        Files.delete(destination);
      }
      return null;
    };
  }

  @Override
  public boolean destinationExists() {
    return Files.exists(this.destination);
  }

  @Override
  public boolean destinationWritable() {
    return Files.isWritable(findExistingPrefix(this.destination));
  }

  private Path findExistingPrefix(Path destination) {
    Path parent = destination.getParent();
    while (parent != null && !Files.exists(parent)) {
      parent = parent.getParent();
    }
    return parent;
  }

  @Override
  public Optional<MD5Checksum> existingFileMd5() {
    if (destinationExists()) {
      try (InputStream is = Files.newInputStream(this.destination);
          BufferedInputStream bis = new BufferedInputStream(is);
          StreamWithMD5Decorator md5Is = StreamWithMD5Decorator.of(bis)) {
        byte[] buffer = new byte[BUFFER];
        while ((md5Is.read(buffer)) != -1) {
        }
        return Optional.of(md5Is.md5());
      } catch (IOException | NoSuchAlgorithmException e) {
        throw new RuntimeException(
            "Could not obtain md5 of the file existing at the target: " + destination,
            e);
      }
    }
    return Optional.empty();
  }

  @Override
  public URI getUri() {
    return URI.create("file://" + destination.toString());
  }
}
