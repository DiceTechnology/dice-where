package technology.dice.dicewhere.downloader.md5;

public class MD5ChecksumResult {
  MD5Checksum originalChecksum;
  MD5Checksum processedChecksum;

  public MD5ChecksumResult(MD5Checksum originalChecksum, MD5Checksum processedChecksum) {
    this.originalChecksum = originalChecksum;
    this.processedChecksum = processedChecksum;
  }

  public boolean checksumMatch() {
    return this.originalChecksum.matches(this.processedChecksum);
  }
}
