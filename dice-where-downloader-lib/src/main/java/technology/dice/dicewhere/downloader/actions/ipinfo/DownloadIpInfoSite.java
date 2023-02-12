package technology.dice.dicewhere.downloader.actions.ipinfo;

import java.net.MalformedURLException;
import java.net.URL;
import technology.dice.dicewhere.downloader.actions.DownloadExecutionResult;
import technology.dice.dicewhere.downloader.destination.FileAcceptor;
import technology.dice.dicewhere.downloader.destination.FileAcceptorFactory;
import technology.dice.dicewhere.downloader.exception.DownloaderException;
import technology.dice.dicewhere.downloader.source.ipinfosite.IpInfoSiteSource;

public class DownloadIpInfoSite extends IpInfoBaseDownload {

  protected final String token;
  protected final String destination;

  public DownloadIpInfoSite(
      boolean noCheckMd5,
      boolean overwrite,
      boolean verbose,
      IpInfoDataset dataset,
      IpInfoFormat format,
      String token,
      String destination) {
    super(noCheckMd5, overwrite, verbose, dataset, format);
    this.token = token;
    this.destination = destination;
  }

  @Override
  public DownloadExecutionResult execute() {
    IpInfoSiteSource ipInfoSiteSource = new IpInfoSiteSource(this.buildContentRemotePath());

    FileAcceptor<?> acceptor =
        FileAcceptorFactory.acceptorFor(
            this.uriForTarget(this.destination, ipInfoSiteSource.fileInfo().getFileName()));

    return this.process(acceptor, ipInfoSiteSource);
  }

  private URL buildContentRemotePath() {
    try {
      return new URL(
          "https",
          "ipinfo.io",
          443,
          "/data/" + dataset.getRemoteName() + "." + format.getSuffix() + "?token=" + token);
    } catch (MalformedURLException e) {
      throw new DownloaderException("Could not compute a valid URL", e);
    }
  }
}
