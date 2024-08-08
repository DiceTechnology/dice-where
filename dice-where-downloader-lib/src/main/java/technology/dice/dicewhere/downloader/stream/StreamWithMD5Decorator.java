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

  private final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
  private final MessageDigest md5;
  DigestInputStream inputStream;
  private boolean consumed = false;
  private Optional<MD5Checksum> md5Checksum = Optional.empty();

  private StreamWithMD5Decorator(DigestInputStream inputStream, MessageDigest md5)
      throws IOException {
    this.inputStream = inputStream;
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
    byte[] data = new byte[8192];
    int bytesRead;
    while ((bytesRead = inputStream.read(data)) != -1) {
      buffer.write(data, 0, bytesRead);
    }
    consumed = true;
  }

  public MD5Checksum md5() {
    if (!consumed) {
      throw new IllegalStateException("Stream not fully consumed yet.");
    }
    return md5Checksum.orElseGet(
        () -> {
          String hex = (new HexBinaryAdapter()).marshal(md5.digest());
          MD5Checksum checksum = MD5Checksum.of(hex);
          md5Checksum = Optional.of(checksum);
          return checksum;
        });
  }

  public InputStream getInputStream() {
    return new ByteArrayInputStream(buffer.toByteArray());
  }

  @Override
  public int read() throws IOException {
    if (!consumed) {
      throw new IllegalStateException("Stream not fully consumed yet.");
    }
    return getInputStream().read();
  }

  @Override
  public int read(byte[] b, int off, int len) throws IOException {
    if (!consumed) {
      throw new IllegalStateException("Stream not fully consumed yet.");
    }
    return getInputStream().read(b, off, len);
  }

  @Override
  public void close() throws IOException {
    getInputStream().close();
    inputStream.close();
  }
  /*
  public static String of1(InputStream inputStream) throws NoSuchAlgorithmException {
    return bytesToHex(checksum(inputStream));
  }

  private static byte[] checksum(InputStream is) {

    MessageDigest md;
    try {
      md = MessageDigest.getInstance("MD5");
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalArgumentException(e);
    }

    try (DigestInputStream dis = new DigestInputStream(is, md)) {
      while (dis.read() != -1)
        ; // empty loop to clear the data
      md = dis.getMessageDigest();
    } catch (IOException e) {
      throw new IllegalArgumentException(e);
    }
    return md.digest();
  }

  private static String bytesToHex(byte[] bytes) {
    StringBuilder sb = new StringBuilder();
    for (byte b : bytes) {
      sb.append(String.format("%02x", b));
    }
    return sb.toString();
  }
  */
}
