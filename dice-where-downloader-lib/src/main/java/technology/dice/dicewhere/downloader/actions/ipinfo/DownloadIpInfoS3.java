package technology.dice.dicewhere.downloader.actions.ipinfo;

import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.utils.Pair;
import technology.dice.dicewhere.downloader.actions.DownloadExecutionResult;
import technology.dice.dicewhere.downloader.actions.S3ClientConfig;
import technology.dice.dicewhere.downloader.destination.FileAcceptor;
import technology.dice.dicewhere.downloader.destination.s3.S3ObjectPath;
import technology.dice.dicewhere.downloader.destination.s3.S3DownloadSetup;
import technology.dice.dicewhere.downloader.source.s3.S3Source;

public class DownloadIpInfoS3 extends IpInfoBaseDownload {
  private static final Logger LOG = LoggerFactory.getLogger(DownloadIpInfoS3.class);
  private final Optional<S3ClientConfig> s3ClientConfig;

  private final String prefix;
  private final String destination;

  private DownloadIpInfoS3(
      Optional<S3ClientConfig> s3ClientConfig,
      boolean noCheckMd5,
      boolean overwrite,
      boolean verbose,
      IpInfoDataset dataset,
      IpInfoFormat format,
      String s3BucketPrefix,
      String destination) {
    super(noCheckMd5, overwrite, verbose, dataset, format);
    this.prefix = s3BucketPrefix;
    this.destination = destination;
    this.s3ClientConfig = s3ClientConfig;
  }

  public DownloadIpInfoS3(
      S3ClientConfig s3ClientConfig,
      boolean noCheckMd5,
      boolean overwrite,
      boolean verbose,
      IpInfoDataset dataset,
      IpInfoFormat format,
      String s3BucketPrefix,
      String destination) {
    this(
        Optional.of(s3ClientConfig),
        noCheckMd5,
        overwrite,
        verbose,
        dataset,
        format,
        s3BucketPrefix,
        destination);
  }

  public DownloadIpInfoS3(
      boolean noCheckMd5,
      boolean overwrite,
      boolean verbose,
      IpInfoDataset dataset,
      IpInfoFormat format,
      String s3BucketPrefix,
      String destination) {
    this(
        Optional.empty(),
        noCheckMd5,
        overwrite,
        verbose,
        dataset,
        format,
        s3BucketPrefix,
        destination);
  }

  @Override
  public DownloadExecutionResult execute() {
    final S3ObjectPath s3ObjectPath = S3ObjectPath.of(prefix);
    S3DownloadSetup downloader = S3DownloadSetup.of(s3ClientConfig);
    final Pair<FileAcceptor, S3Source> download =
        downloader.setupDownload(this.destination, s3ObjectPath, this.ipInfoPath());
    return this.process(download.left(), download.right());
  }
}
