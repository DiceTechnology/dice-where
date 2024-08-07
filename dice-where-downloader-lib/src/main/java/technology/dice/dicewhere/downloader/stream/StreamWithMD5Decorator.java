package technology.dice.dicewhere.downloader.stream;

import java.io.IOException;
import java.io.InputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import javax.xml.bind.annotation.adapters.HexBinaryAdapter;
import technology.dice.dicewhere.downloader.md5.MD5Checksum;

public class StreamWithMD5Decorator extends InputStream {

  private final DigestInputStream inputStream;
  private final MessageDigest md5;

  private StreamWithMD5Decorator(DigestInputStream inputStream, MessageDigest md5) {
    this.inputStream = inputStream;
    this.md5 = md5;
    inputStream.on(false);
  }


  public static StreamWithMD5Decorator of(InputStream inputStream) throws NoSuchAlgorithmException {
    MessageDigest md5 = MessageDigest.getInstance("MD5");
    DigestInputStream dis = new DigestInputStream(inputStream, md5);
    return new StreamWithMD5Decorator(dis, md5);
  }

  public MD5Checksum md5() {
    String hex = (new HexBinaryAdapter()).marshal(md5.digest());
    return MD5Checksum.of(hex);
  }

  @Override
  public int read() throws IOException {
    return inputStream.read();
  }

  @Override
  public void close() throws IOException {
    inputStream.close();
  }
}
