package technology.dice.dicewhere.downloader.stream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import javax.xml.bind.annotation.adapters.HexBinaryAdapter;
import technology.dice.dicewhere.downloader.md5.MD5Checksum;

public class StreamWithMD5Decorator extends InputStream {

  private static final int BUFFER_SIZE = 8192;
  private static final HexBinaryAdapter HEX_BINARY_ADAPTER = new HexBinaryAdapter();

  private final MessageDigest md5;
  private final DigestInputStream originalStream;
  private final MD5Checksum md5Checksum;

  private InputStream duplicatedStream;

  private StreamWithMD5Decorator(DigestInputStream originalStream, MessageDigest md5)
      throws IOException {
    this.originalStream = originalStream;
    this.md5 = md5;
    consumeStream();
    md5Checksum = generateMd5();
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
    duplicatedStream = new ByteArrayInputStream(tempBufferStream.toByteArray());
  }

  private MD5Checksum generateMd5() {
    String hex = HEX_BINARY_ADAPTER.marshal(md5.digest());
    return MD5Checksum.of(hex);
  }

  public MD5Checksum md5() {
    return md5Checksum;
  }

  @Override
  public int read() throws IOException {
    return duplicatedStream.read();
  }

  @Override
  public int read(byte[] b, int off, int len) throws IOException {
    return duplicatedStream.read(b, off, len);
  }

  @Override
  public void close() throws IOException {
    duplicatedStream.close();
    originalStream.close();
  }
}
