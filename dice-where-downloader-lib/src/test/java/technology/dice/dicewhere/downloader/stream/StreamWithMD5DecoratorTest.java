package technology.dice.dicewhere.downloader.stream;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.github.tomakehurst.wiremock.matching.UrlPattern;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
import junit.framework.TestCase;
import org.apache.commons.io.IOUtils;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.internal.runners.JUnit4ClassRunner;
import org.junit.runner.RunWith;

@RunWith(JUnit4ClassRunner.class)
public class StreamWithMD5DecoratorTest extends TestCase {

  private static final String PATH = "/maxmind/maxmind-city-1.zip";

  @ClassRule static WireMockRule wireMockRule = new WireMockRule(wireMockConfig().dynamicPort());

  @BeforeClass
  public static void beforeClass() {
    wireMockRule.start();
  }

  @Test
  public void shouldSuccessfullyReadAndCalculateDigestOfStream()
      throws IOException, NoSuchAlgorithmException, URISyntaxException {
    Path path = Path.of(getClass().getResource(PATH).toURI());
    StreamWithMD5Decorator is = StreamWithMD5Decorator.of(new FileInputStream(path.toFile()));

    // Exhaust stream for the complete hash digest
    byte[] buffer = new byte[8192];
    while ((is.read(buffer)) != -1) {}

    String first = is.md5().stringFormat();
    IOUtils.toString(is, Charset.defaultCharset());

    // Assert the Stream Hash before and after
    assertEquals(first, is.md5().stringFormat());
    assertEquals(first, "9c7dd68c8352f1c59a33efe0dca04f06");
  }

  @Test
  public void shouldSuccessfullyReadAndCalculateDigestOfStreamFromHttp()
      throws IOException, NoSuchAlgorithmException, URISyntaxException {
    Path path = Path.of(getClass().getResource(PATH).toURI());

    wireMockRule.stubFor(
        WireMock.get(UrlPattern.ANY)
            .willReturn(
                aResponse().withBody(IOUtils.toByteArray(new FileInputStream(path.toFile())))));

    URL url = new URL("http://localhost:" + wireMockRule.port() + "/data/file.mdb");
    HttpURLConnection connection = (HttpURLConnection) url.openConnection();

    StreamWithMD5Decorator is = StreamWithMD5Decorator.of(connection.getInputStream());
    // Exhaust stream for the complete hash digest
    byte[] buffer = new byte[8192];
    while ((is.read(buffer)) != -1) {}

    String first = is.md5().stringFormat();
    // Read from the stream
    IOUtils.toString(is, Charset.defaultCharset());

    // Assert the Stream Hash before and after
    assertEquals(first, is.md5().stringFormat());
    assertEquals(first, "9c7dd68c8352f1c59a33efe0dca04f06");
  }
}
