package technology.dice.dicewhere.downloader.source;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.NoSuchAlgorithmException;
import technology.dice.dicewhere.downloader.destination.FileAcceptor;
import technology.dice.dicewhere.downloader.exception.DownloaderException;
import technology.dice.dicewhere.downloader.files.FileInfo;
import technology.dice.dicewhere.downloader.md5.MD5Checksum;
import technology.dice.dicewhere.downloader.stream.StreamWithMD5Decorator;

public abstract class BaseUrlSource implements FileSource {

  protected FileInfo fileInfo;
  protected final URL dataFileLocation;

  protected BaseUrlSource(URL dataFileLocation) {
    this.dataFileLocation = dataFileLocation;
  }

  @Override
  public MD5Checksum produce(FileAcceptor acceptor, boolean noMd5Check) {
    try {
      HttpURLConnection httpConnection = (HttpURLConnection) this.dataFileLocation.openConnection();
      httpConnection.setRequestMethod("GET");

      try (StreamWithMD5Decorator is = StreamWithMD5Decorator.of(httpConnection.getInputStream())) {
        acceptor
            .getStreamConsumer(fileInfo.getMd5Checksum(), fileInfo.getTimestamp(), noMd5Check)
            .consume(is, fileInfo.getSize());
        return is.md5();
      }
    } catch (IOException | NoSuchAlgorithmException e) {
      throw new DownloaderException("Could not read file at " + fileInfo.getUri().toString(), e);
    }
  }
}
