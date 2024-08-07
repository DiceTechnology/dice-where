package technology.dice.dicewhere.downloader.stream;

import static org.junit.Assert.assertEquals;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
import org.apache.commons.io.IOUtils;
import org.junit.Test;


public class StreamWithMD5DecoratorTest {

  private static final String PATH = "/maxmind/maxmind-city-1.zip";

  @Test
  public void shouldSuccessfullyReadAndCalculateDigestOfStream()
      throws IOException, NoSuchAlgorithmException, URISyntaxException {
    Path path = Path.of(getClass().getResource(PATH).toURI());
    StreamWithMD5Decorator is = StreamWithMD5Decorator.of(new FileInputStream(path.toFile()));

    String first = is.md5().stringFormat();
    //Read from the stream
    IOUtils.toString(is, Charset.defaultCharset());

    //Calculate multiple Hashes
    is.md5().stringFormat();
    is.md5().stringFormat();

    //Assert the Stream Hash before and after
    assertEquals(first, is.md5().stringFormat());
  }
}
