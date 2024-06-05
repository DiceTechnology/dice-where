package technology.dice.dicewhere.downloader.source;

import technology.dice.dicewhere.downloader.files.FileInfo;
import technology.dice.dicewhere.downloader.md5.MD5Checksum;
import technology.dice.dicewhere.downloader.destination.FileAcceptor;

public interface FileSource {

  FileInfo fileInfo();

  MD5Checksum produce(FileAcceptor consumer, boolean noMd5Check);
}
