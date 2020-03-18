package technology.dice.dicewhere.downloader.destination;

import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Clock;
import software.amazon.awssdk.services.s3.S3Client;
import technology.dice.dicewhere.downloader.ObjectMapperInstance;
import technology.dice.dicewhere.downloader.PathUtils;
import technology.dice.dicewhere.downloader.destination.local.LocalFileAcceptor;
import technology.dice.dicewhere.downloader.destination.s3.S3FileAcceptor;

public class FileAcceptorFactory {

  public static FileAcceptor acceptorFor(Path destination) {
    return new LocalFileAcceptor(destination);
  }

  public static FileAcceptor acceptorFor(URI destination) {
    switch (destination.getScheme() == null ? "file" : destination.getScheme().toLowerCase()) {
      case "file":
        return new LocalFileAcceptor(
            destination.getScheme() == null
                ? Paths.get(destination.toString())
                : Paths.get(destination));
      case "s3":
        return new S3FileAcceptor(
            S3Client.create(),
            destination.getHost(),
            PathUtils.removeLeadingCharacter(
                PathUtils.removeTrailingCharacter(destination.getPath(), "/"), "/"),
            ObjectMapperInstance.INSTANCE,
            Clock.systemUTC());
      default:
        throw new IllegalArgumentException("Unsupported scheme" + destination.getScheme());
    }
  }
}
