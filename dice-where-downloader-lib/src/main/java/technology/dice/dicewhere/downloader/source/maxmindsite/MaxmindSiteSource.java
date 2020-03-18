package technology.dice.dicewhere.downloader.source.maxmindsite;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;
import technology.dice.dicewhere.downloader.destination.FileAcceptor;
import technology.dice.dicewhere.downloader.exception.DownloaderException;
import technology.dice.dicewhere.downloader.files.FileInfo;
import technology.dice.dicewhere.downloader.files.UrlFileInfo;
import technology.dice.dicewhere.downloader.md5.MD5Checksum;
import technology.dice.dicewhere.downloader.source.FileSource;
import technology.dice.dicewhere.downloader.stream.StreamWithMD5Decorator;

public class MaxmindSiteSource implements FileSource {
  private final URL dataFileLocation;
  private final URL md5FileLocation;
  private FileInfo fileInfo;

  public MaxmindSiteSource(URL dataFileLocation, URL md5FileLocation) {
    this.dataFileLocation = dataFileLocation;
    this.md5FileLocation = md5FileLocation;
  }

  @Override
  public synchronized FileInfo fileInfo() {
    if (this.fileInfo == null) {
      try {
        HttpURLConnection dataConnection =
            (HttpURLConnection) this.dataFileLocation.openConnection();
        dataConnection.setRequestMethod("HEAD");

        if (dataConnection.getResponseCode() > 299 || dataConnection.getResponseCode() < 200) {
          throw new DownloaderException("Could not find remote file");
        }

        long fileSize = dataConnection.getContentLengthLong();
        String contentDisposition = dataConnection.getHeaderField("Content-Disposition");

        if (contentDisposition == null || contentDisposition.indexOf("=") == -1) {
          throw new DownloaderException("Cannot determine remote file name");
        }

        String fileName = contentDisposition.split("=")[1];
        fileName = fileName.replaceAll("\"", "").replaceAll("]", "");

        HttpURLConnection md5Connection = (HttpURLConnection) this.md5FileLocation.openConnection();
        md5Connection.setRequestMethod("GET");
        try (InputStream is = md5Connection.getInputStream()) {
          final Scanner scanner = new Scanner(is).useDelimiter("\\A");
          String md5 = scanner.next();
          this.fileInfo =
              new UrlFileInfo(
                  this.dataFileLocation,
                  fileName,
                  this.extractDateFromFilename(fileName),
                  MD5Checksum.of(md5),
                  fileSize);
        }
      } catch (IOException e) {
        throw new DownloaderException(e);
      }
    }
    return this.fileInfo;
  }

  private Instant extractDateFromFilename(String fileName) {
    return LocalDate.parse(
            fileName.substring(fileName.lastIndexOf("_") + 1, fileName.lastIndexOf("_") + 1 + 8),
            DateTimeFormatter.BASIC_ISO_DATE)
        .atStartOfDay()
        .toInstant(ZoneOffset.UTC);
  }

  @Override
  public MD5Checksum produce(FileAcceptor acceptor) {
    try {
      HttpURLConnection httpConnection = (HttpURLConnection) this.dataFileLocation.openConnection();
      httpConnection.setRequestMethod("GET");

      try (StreamWithMD5Decorator is = StreamWithMD5Decorator.of(httpConnection.getInputStream())) {
        acceptor
            .getStreamConsumer(fileInfo.getMd5Checksum(), fileInfo.getTimestamp())
            .consume(is, fileInfo.getSize());
        return is.md5();
      }
    } catch (IOException | NoSuchAlgorithmException e) {
      throw new DownloaderException("Could not read file at " + fileInfo.getUri().toString(), e);
    }
  }
}
