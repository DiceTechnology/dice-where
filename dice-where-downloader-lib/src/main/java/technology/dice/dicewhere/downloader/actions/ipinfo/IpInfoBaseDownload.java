package technology.dice.dicewhere.downloader.actions.ipinfo;

import technology.dice.dicewhere.downloader.Download;

public abstract class IpInfoBaseDownload extends Download {

  protected final IpInfoDataset dataset;

  protected final IpInfoFormat format;

  public IpInfoBaseDownload(
      boolean noCheckMd5,
      boolean overwrite,
      boolean verbose,
      IpInfoDataset dataset,
      IpInfoFormat format) {
    super(noCheckMd5, overwrite, verbose);
    this.dataset = dataset;
    this.format = format;
  }

  protected String ipInfoPath() {
    return "ipinfo/" + dataset.getRemoteName() + "/" + format.getSuffix();
  }
}
