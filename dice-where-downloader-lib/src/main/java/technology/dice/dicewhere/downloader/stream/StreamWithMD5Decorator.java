package technology.dice.dicewhere.downloader.stream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;
import javax.xml.bind.annotation.adapters.HexBinaryAdapter;
import technology.dice.dicewhere.downloader.md5.MD5Checksum;

public class StreamWithMD5Decorator extends InputStream {

  private static final int BUFFER_SIZE = 8192;
  private static final HexBinaryAdapter HEX_BINARY_ADAPTER = new HexBinaryAdapter();

  private final MessageDigest md5;
  private final DigestInputStream originalStream;

  private Optional<InputStream> duplicatedStream = Optional.empty();
  private Optional<MD5Checksum> md5Checksum = Optional.empty();

  private StreamWithMD5Decorator(DigestInputStream originalStream, MessageDigest md5)
      throws IOException {
    this.originalStream = originalStream;
    this.md5 = md5;
    consumeStream();
  }

  public static StreamWithMD5Decorator of(InputStream inputStream)
      throws NoSuchAlgorithmException, IOException {
    MessageDigest md5 = MessageDigest.getInstance("MD5");
    DigestInputStream dis = new DigestInputStream(inputStream, md5);
    return new StreamWithMD5Decorator(dis, md5);
  }

  private void consumeStream() throws IOException {
    ByteArrayOutputStream tempBufferStream = new ByteArrayOutputStream();
    byte[] data = new byte[BUFFER_SIZE];
    int bytesRead;
    while ((bytesRead = originalStream.read(data)) != -1) {
      tempBufferStream.write(data, 0, bytesRead);
    }
    duplicatedStream = Optional.of(new ByteArrayInputStream(tempBufferStream.toByteArray()));
  }

  public MD5Checksum md5() {
    if (duplicatedStream.isEmpty()) {
      throw new IllegalStateException("Stream not fully consumed yet.");
    }
    return md5Checksum.orElseGet(
        () -> {
          String hex = HEX_BINARY_ADAPTER.marshal(md5.digest());
          MD5Checksum checksum = MD5Checksum.of(hex);
          md5Checksum = Optional.of(checksum);
          return checksum;
        });
  }

  private InputStream inputStream() {
    return duplicatedStream.orElseThrow(
        () -> new IllegalStateException("Stream not fully consumed yet."));
  }

  @Override
  public int read() throws IOException {
    return inputStream().read();
  }

  @Override
  public int read(byte[] b, int off, int len) throws IOException {
    return inputStream().read(b, off, len);
  }

  @Override
  public void close() throws IOException {
    inputStream().close();
    originalStream.close();
  }
}
