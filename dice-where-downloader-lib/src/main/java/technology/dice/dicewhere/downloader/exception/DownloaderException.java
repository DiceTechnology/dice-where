package technology.dice.dicewhere.downloader.exception;

public class DownloaderException extends RuntimeException {

  public DownloaderException(Throwable cause) {
    super(cause);
  }

  public DownloaderException(String message) {
    super(message);
  }

  public DownloaderException(String s, Exception e) {
    super(s, e);
  }
}
