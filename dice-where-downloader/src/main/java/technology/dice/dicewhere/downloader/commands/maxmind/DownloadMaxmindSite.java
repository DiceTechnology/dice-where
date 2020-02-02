package technology.dice.dicewhere.downloader.commands.maxmind;

import java.net.MalformedURLException;
import java.net.URL;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import technology.dice.dicewhere.downloader.commands.CommandExecutionResult;
import technology.dice.dicewhere.downloader.destination.FileAcceptor;
import technology.dice.dicewhere.downloader.destination.FileAcceptorFactory;
import technology.dice.dicewhere.downloader.exception.DownloaderException;
import technology.dice.dicewhere.downloader.source.maxmindsite.MaxmindSiteSource;

@Command(
    name = "maxmind-site",
    description = "Downloads the selected Maxmind edition of a database from Maxmind's website")
public class DownloadMaxmindSite extends MaxmindBaseCommand {

  @Option(
      names = {"-k", "--key"},
      required = true,
      description = "The maxmind download key")
  String key;

  @Parameters(
      index = "0",
      description =
          "The destination of the file. Must start with the scheme (s3:// or file://). S3 destinations require credentials to be provided through the default chain for AWS Java SDK. See https://docs.aws.amazon.com/sdk-for-java/v2/developer-guide/credentials.html for more details")
  String destination;

  /** Default constructor to be used by picocli when running from the command line */
  public DownloadMaxmindSite() {
    super();
  }

  /** Constructor to be used when running programmatically */
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
  public CommandExecutionResult execute() {
    MaxmindSiteSource maxmindSource =
        new MaxmindSiteSource(this.buildContentRemotePath(), this.buildMd5RemotePath());

    FileAcceptor<?, ?> acceptor =
        FileAcceptorFactory.acceptorFor(
            this.uriForTarget(this.destination, maxmindSource.fileInfo().getFileName()));

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
