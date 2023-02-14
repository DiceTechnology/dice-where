package technology.dice.dicewhere.downloader.actions.maxmind;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import technology.dice.dicewhere.downloader.actions.DownloadExecutionResult;
import technology.dice.dicewhere.downloader.destination.FileAcceptor;
import technology.dice.dicewhere.downloader.destination.FileAcceptorFactory;
import technology.dice.dicewhere.downloader.exception.DownloaderException;
import technology.dice.dicewhere.downloader.source.maxmindsite.MaxmindSiteSource;

public class DownloadMaxmindSite extends MaxmindBaseDownload {

  protected final String key;
  protected final String destination;

  public DownloadMaxmindSite(
      boolean noCheckMd5,
      boolean overwrite,
      boolean verbose,
      MaxmindEdition edition,
      MaxmindDatabase database,
      MaxmindFormat format,
      String key,
      String destination) {
    super(noCheckMd5, overwrite, verbose, edition, database, format);
    this.key = key;
    this.destination = destination;
  }

  @Override
  public DownloadExecutionResult execute() {
    MaxmindSiteSource maxmindSource =
        new MaxmindSiteSource(this.buildContentRemotePath(), this.buildMd5RemotePath());

    FileAcceptor<?> acceptor =
        FileAcceptorFactory.acceptorFor(
            URI.create(
                this.destination
                    + "/"
                    + this.maxmindPath()
                    + "/"
                    + maxmindSource.fileInfo().getFileName()));

    return this.process(acceptor, maxmindSource);
  }

  private URL buildContentRemotePath() {
    try {
      return new URL(
          "https",
          "download.maxmind.com",
          443,
          "/app/geoip_download?edition_id="
              + edition.name()
              + "-"
              + database.getRemoteName()
              + format.getRemoteName()
              + "&license_key="
              + key
              + "&suffix="
              + format.getSuffix());
    } catch (MalformedURLException e) {
      throw new DownloaderException("Could not compute a valid URL", e);
    }
  }

  private URL buildMd5RemotePath() {
    try {
      return new URL(
          "https",
          "download.maxmind.com",
          443,
          "/app/geoip_download?edition_id="
              + edition.name()
              + "-"
              + database.getRemoteName()
              + format.getRemoteName()
              + "&license_key="
              + key
              + "&suffix="
              + format.getSuffix()
              + ".md5");
    } catch (MalformedURLException e) {
      throw new DownloaderException("Could not compute a valid URL", e);
    }
  }
}
