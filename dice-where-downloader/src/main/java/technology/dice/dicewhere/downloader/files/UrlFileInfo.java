package technology.dice.dicewhere.downloader.files;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import technology.dice.dicewhere.downloader.md5.MD5Checksum;

public class UrlFileInfo implements FileInfo {
  private final URL location;
  private final String fileName;
  private MD5Checksum md5Checksum;
  private long size;

  public UrlFileInfo(URL location, String fileName, MD5Checksum md5Checksum, long size) {
    this.location = location;
    this.md5Checksum = md5Checksum;
    this.size = size;
    this.fileName = fileName;
  }

  public MD5Checksum getMd5Checksum() {
    return md5Checksum;
  }

  @Override
  public URI getUri() {
    try {
      return this.location.toURI();
    } catch (URISyntaxException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public long getSize() {
    return size;
  }

  @Override
  public String getFileName() {
    return this.fileName;
  }
}
