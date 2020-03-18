package technology.dice.dicewhere.downloader.files;

import java.net.URI;
import java.time.Instant;
import technology.dice.dicewhere.downloader.md5.MD5Checksum;

public interface FileInfo {
  MD5Checksum getMd5Checksum();

  URI getUri();

  long getSize();

  String getFileName();

  Instant getTimestamp();
}
