package technology.dice.dicewhere.downloader.destination.local;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.junit.Assert.assertNotEquals;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.github.tomakehurst.wiremock.matching.UrlPattern;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Random;
import javax.xml.bind.annotation.adapters.HexBinaryAdapter;
import junit.framework.TestCase;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpStatus;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.internal.runners.JUnit4ClassRunner;
import org.junit.runner.RunWith;
import org.testcontainers.shaded.org.apache.commons.lang3.tuple.Pair;
import technology.dice.dicewhere.downloader.files.FileInfo;
import technology.dice.dicewhere.downloader.source.ipinfosite.IpInfoSiteSource;

@RunWith(JUnit4ClassRunner.class)
public class LocalFileAcceptorTest extends TestCase {

  private static final int TEST_FILE_SIZE = 1024 * 1024;
  @ClassRule
  static WireMockRule wireMockRule = new WireMockRule(wireMockConfig().dynamicPort());

  @BeforeClass
  public static void beforeClass() {
    wireMockRule.start();
  }

  @Test
  public void corruptedFileEmptyPreexistingSet() throws IOException, NoSuchAlgorithmException {
    Pair<Path, String> tempFile = generateTempFile();
    Path destinationDir = Files.createTempDirectory("dice-where");
    IpInfoSiteSource ipInfoSiteSource = new IpInfoSiteSource(
        new URL("http://localhost:" + wireMockRule.port() + "/data/file.mdb"));
    wireMockRule.stubFor(WireMock.head(UrlPattern.ANY).willReturn(
        aResponse().withStatus(HttpStatus.SC_OK)
            .withHeader("Etag", "aaa")
            .withHeader("Content-Length", Long.toString(TEST_FILE_SIZE))
            .withHeader("Last-Modified", "Thu, 01 Dec 1994 16:00:00 GMT")));
    wireMockRule.stubFor(WireMock.get(UrlPattern.ANY)
        .willReturn(aResponse().withBody(
            IOUtils.toByteArray(new FileInputStream(tempFile.getLeft().toFile())))));

    FileInfo fileInfo = ipInfoSiteSource.fileInfo();
    ipInfoSiteSource.produce(new LocalFileAcceptor(destinationDir.resolve("file.mdb")), false);
    assertNotEquals(tempFile.getRight().toLowerCase(), fileInfo.getMd5Checksum().stringFormat());
    assertFalse(Files.list(destinationDir).findAny().isPresent());
  }

  @Test
  public void corruptedFilePreexistingSet() throws IOException, NoSuchAlgorithmException {
    Pair<Path, String> tempFile = generateTempFile();
    Pair<Path, String> existingFile = generateTempFile();
    Path destinationDir = Files.createTempDirectory("dice-where");
    Files.copy(existingFile.getLeft(), destinationDir.resolve("existingFile.mdb"));
    IpInfoSiteSource ipInfoSiteSource = new IpInfoSiteSource(
        new URL("http://localhost:" + wireMockRule.port() + "/data/file.mdb"));
    wireMockRule.stubFor(WireMock.head(UrlPattern.ANY).willReturn(
        aResponse().withStatus(HttpStatus.SC_OK)
            .withHeader("Etag", "aaa")
            .withHeader("Content-Length", Long.toString(TEST_FILE_SIZE))
            .withHeader("Last-Modified", "Thu, 01 Dec 1994 16:00:00 GMT")));
    wireMockRule.stubFor(WireMock.get(UrlPattern.ANY)
        .willReturn(aResponse().withBody(
            IOUtils.toByteArray(new FileInputStream(tempFile.getLeft().toFile())))));

    FileInfo fileInfo = ipInfoSiteSource.fileInfo();
    ipInfoSiteSource.produce(new LocalFileAcceptor(destinationDir.resolve("file.mdb")), false);
    assertNotEquals(tempFile.getRight().toLowerCase(), fileInfo.getMd5Checksum().stringFormat());
    assertEquals(Files.list(destinationDir).count(), 1);
    assertFalse(Files.exists(destinationDir.resolve("file.mdb")));
    assertTrue(Arrays.equals(Files.readAllBytes(existingFile.getLeft()),
        Files.readAllBytes(Files.list(destinationDir).findFirst().get())));
  }

  @Test
  public void goodFileEmptyPreexistingSet() throws IOException, NoSuchAlgorithmException {
    Pair<Path, String> tempFile = generateTempFile();
    Path destinationDir = Files.createTempDirectory("dice-where");
    IpInfoSiteSource ipInfoSiteSource = new IpInfoSiteSource(
        new URL("http://localhost:" + wireMockRule.port() + "/data/file.mdb"));
    wireMockRule.stubFor(WireMock.head(UrlPattern.ANY).willReturn(
        aResponse().withStatus(HttpStatus.SC_OK)
            .withHeader("Etag", tempFile.getRight())
            .withHeader("Content-Length", Long.toString(TEST_FILE_SIZE))
            .withHeader("Last-Modified", "Thu, 01 Dec 1994 16:00:00 GMT")));
    wireMockRule.stubFor(WireMock.get(UrlPattern.ANY)
        .willReturn(aResponse().withBody(
            IOUtils.toByteArray(new FileInputStream(tempFile.getLeft().toFile())))));

    FileInfo fileInfo = ipInfoSiteSource.fileInfo();
    ipInfoSiteSource.produce(new LocalFileAcceptor(destinationDir.resolve("file.mdb")), false);
    assertEquals(tempFile.getRight().toLowerCase(), fileInfo.getMd5Checksum().stringFormat());
    assertEquals(1, Files.list(destinationDir).count());
    assertTrue(Arrays.equals(Files.readAllBytes(tempFile.getLeft()),
        Files.readAllBytes(destinationDir.resolve("file.mdb"))));
  }

  @Test
  public void goodFilePreexistingSet() throws IOException, NoSuchAlgorithmException {
    Pair<Path, String> tempFile = generateTempFile();
    Pair<Path, String> existingFile = generateTempFile();
    Path destinationDir = Files.createTempDirectory("dice-where");
    Files.copy(existingFile.getLeft(), destinationDir.resolve("existingFile.mdb"));
    IpInfoSiteSource ipInfoSiteSource = new IpInfoSiteSource(
        new URL("http://localhost:" + wireMockRule.port() + "/data/file.mdb"));
    wireMockRule.stubFor(WireMock.head(UrlPattern.ANY).willReturn(
        aResponse().withStatus(HttpStatus.SC_OK)
            .withHeader("Etag", tempFile.getRight())
            .withHeader("Content-Length", Long.toString(TEST_FILE_SIZE))
            .withHeader("Last-Modified", "Thu, 01 Dec 1994 16:00:00 GMT")));
    wireMockRule.stubFor(WireMock.get(UrlPattern.ANY)
        .willReturn(aResponse().withBody(
            IOUtils.toByteArray(new FileInputStream(tempFile.getLeft().toFile())))));

    FileInfo fileInfo = ipInfoSiteSource.fileInfo();
    ipInfoSiteSource.produce(new LocalFileAcceptor(destinationDir.resolve("file.mdb")), false);
    assertEquals(tempFile.getRight().toLowerCase(), fileInfo.getMd5Checksum().stringFormat());
    assertEquals(2, Files.list(destinationDir).count());
    assertTrue(Arrays.equals(Files.readAllBytes(tempFile.getLeft()),
        Files.readAllBytes(destinationDir.resolve("file.mdb"))));
  }

  private Pair<Path, String> generateTempFile() throws IOException, NoSuchAlgorithmException {
    byte[] contents = new byte[TEST_FILE_SIZE];
    new Random().nextBytes(contents);
    Path tempFile = Files.createTempFile("dice-where", "downloader");
    Files.write(tempFile, contents);
    MessageDigest md = MessageDigest.getInstance("MD5");
    String hex = (new HexBinaryAdapter()).marshal(md.digest(contents));
    return Pair.of(tempFile, hex);
  }
}