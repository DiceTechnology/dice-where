package technology.dice.dicewhere.downloader.source.ipinfosite;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import technology.dice.dicewhere.downloader.exception.DownloaderException;
import technology.dice.dicewhere.downloader.files.FileInfo;
import technology.dice.dicewhere.downloader.files.UrlFileInfo;
import technology.dice.dicewhere.downloader.md5.MD5Checksum;
import technology.dice.dicewhere.downloader.source.BaseUrlSource;

public class IpInfoSiteSource extends BaseUrlSource {

  private static final String PATTERN_FORMAT = "yyyyMMdd";

  DateTimeFormatter formatter =
      DateTimeFormatter.ofPattern(PATTERN_FORMAT).withZone(ZoneId.systemDefault());

  public IpInfoSiteSource(URL dataFileLocation) {
    super(dataFileLocation);
  }

  @Override
  public FileInfo fileInfo() {
    if (this.fileInfo == null) {
      try {
        HttpURLConnection dataConnection =
            (HttpURLConnection) this.dataFileLocation.openConnection();
        dataConnection.setRequestMethod("HEAD");

        if (dataConnection.getResponseCode() > 299 || dataConnection.getResponseCode() < 200) {
          throw new DownloaderException("Could not find remote file");
        }

        long fileSize = dataConnection.getContentLengthLong();
        String etag = dataConnection.getHeaderField("etag").replaceAll("\"", "");
        String date = dataConnection.getHeaderField("last-modified");
        final Instant lastModified = Instant.from(DateTimeFormatter.RFC_1123_DATE_TIME.parse(date));

        String fileName = Paths.get(dataFileLocation.getPath()).getFileName().toString();
        int lastDotIndex = fileName.lastIndexOf('.');

        this.fileInfo =
            new UrlFileInfo(
                this.dataFileLocation,
                fileName.substring(0, lastDotIndex)
                    + "-"
                    + formatter.format(lastModified)
                    + fileName.substring(lastDotIndex),
                lastModified,
                MD5Checksum.of(etag),
                fileSize);

      } catch (IOException e) {
        throw new DownloaderException(e);
      }
    }
    return this.fileInfo;
  }
}
