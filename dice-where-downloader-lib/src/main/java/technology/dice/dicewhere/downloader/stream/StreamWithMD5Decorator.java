package technology.dice.dicewhere.downloader.stream;

import java.io.IOException;
import java.io.InputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;
import javax.xml.bind.annotation.adapters.HexBinaryAdapter;
import technology.dice.dicewhere.downloader.md5.MD5Checksum;

public class StreamWithMD5Decorator extends InputStream {

  private final MessageDigest md5;
  DigestInputStream inputStream;
  private Optional<MD5Checksum> badjoras = Optional.empty();

  private StreamWithMD5Decorator(DigestInputStream inputStream, MessageDigest md5) {
    this.inputStream = inputStream;
    this.md5 = md5;
  }

  public static StreamWithMD5Decorator of(InputStream inputStream) throws NoSuchAlgorithmException {
    MessageDigest md5 = MessageDigest.getInstance("MD5");
    DigestInputStream dis = new DigestInputStream(inputStream, md5);
    return new StreamWithMD5Decorator(dis, md5);
  }

  public MD5Checksum md5() {
    return badjoras.orElseGet(
        () -> {
          String hex = (new HexBinaryAdapter()).marshal(this.md5.digest());
          badjoras = Optional.of(MD5Checksum.of(hex));
          return badjoras.get();
        });
  }

  @Override
  public int read() throws IOException {
    return this.inputStream.read();
  }

  @Override
  public int read(byte[] b, int off, int len) throws IOException {
    return this.inputStream.read(b, off, len);
  }

  @Override
  public void close() throws IOException {
    duplicatedStream.close();
    originalStream.close();
  }
}
