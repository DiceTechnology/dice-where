package technology.dice.dicewhere.downloader.source.ipinfosite;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertThrows;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import java.time.Clock;
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
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.containers.localstack.LocalStackContainer.Service;
import org.testcontainers.containers.wait.strategy.HttpWaitStrategy;
import org.testcontainers.shaded.org.apache.commons.lang3.tuple.Pair;
import org.testcontainers.utility.DockerImageName;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.utils.StringInputStream;
import technology.dice.dicewhere.downloader.ObjectMapperInstance;
import technology.dice.dicewhere.downloader.destination.s3.S3FileAcceptor;
import technology.dice.dicewhere.downloader.files.FileInfo;

@RunWith(JUnit4ClassRunner.class)
public class IpInfoSiteSourceTest extends TestCase {

  private static final int TEST_FILE_SIZE = 1024 * 1024;
  private static final String TEST_BUCKET = "test-bucket";
  private static final String TEST_KEY = "downloads/test-file";
  private static final String LATEST_KEY = "downloads/latest";

  private static final String MOCK_LATEST =
          "{\"uploadedAt\":\"2024-07-08T14:04:39.047489Z\",\"key\":\"downloads/mock-latest\"}";

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  @ClassRule
  static WireMockRule wireMockRule = new WireMockRule(wireMockConfig().dynamicPort());
  @ClassRule
  static LocalStackContainer localStack = new LocalStackContainer(
      DockerImageName.parse("localstack/localstack:3.0")).withServices(Service.S3)
      .waitingFor(new HttpWaitStrategy());

  private static S3Client S3_CLIENT;

  @BeforeClass
  public static void beforeClass() {
    localStack.start();
    wireMockRule.start();

    S3_CLIENT = S3Client.builder().endpointOverride(localStack.getEndpoint()).credentialsProvider(
            StaticCredentialsProvider.create(
                AwsBasicCredentials.create(localStack.getAccessKey(), localStack.getSecretKey())))
        .region(Region.of(localStack.getRegion())).build();
    S3_CLIENT.createBucket(CreateBucketRequest.builder().bucket(TEST_BUCKET).build());
    PutObjectRequest putLatest =
            PutObjectRequest.builder()
                    .key(LATEST_KEY)
                    .bucket(TEST_BUCKET)
                    .contentLength((long) MOCK_LATEST.length())
                    .build();
    S3_CLIENT.putObject(
            putLatest,
            RequestBody.fromInputStream(
                    new StringInputStream(MOCK_LATEST), MOCK_LATEST.length()));
  }

  @Test
  public void corruptedFile() throws IOException, NoSuchAlgorithmException {
    final Pair<Path, String> tempFile = generateTempFile();
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

    final FileInfo fileInfo = ipInfoSiteSource.fileInfo();
    ipInfoSiteSource.produce(
        new S3FileAcceptor(S3_CLIENT, TEST_BUCKET, TEST_KEY, ObjectMapperInstance.INSTANCE,
            Clock.systemUTC()), false);
    // verify no s3 file
    assertNotEquals(tempFile.getRight(), fileInfo.getMd5Checksum().stringFormat());
    assertThrows(NoSuchKeyException.class, () -> S3_CLIENT.getObject(
        GetObjectRequest.builder()
            .bucket(TEST_BUCKET)
            .key(TEST_KEY)
            .build()));
    // verify latest contents did not change
    String latest = S3_CLIENT.getObjectAsBytes(
            GetObjectRequest.builder()
                    .bucket(TEST_BUCKET)
                    .key(LATEST_KEY)
                    .build()).asUtf8String();
    assertEquals(latest, MOCK_LATEST);
  }

  @Test
  public void goodFile() throws IOException, NoSuchAlgorithmException {
    final Pair<Path, String> tempFile = generateTempFile();
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

    final FileInfo fileInfo = ipInfoSiteSource.fileInfo();
    ipInfoSiteSource.produce(
        new S3FileAcceptor(S3_CLIENT, TEST_BUCKET, TEST_KEY, ObjectMapperInstance.INSTANCE,
            Clock.systemUTC()), false);
    assertEquals(fileInfo.getMd5Checksum().stringFormat().toLowerCase(),
        tempFile.getRight().toLowerCase());
    final ResponseInputStream<GetObjectResponse> object = S3_CLIENT.getObject(
        GetObjectRequest.builder()
            .bucket(TEST_BUCKET)
            .key(TEST_KEY)
            .build());
    // verify s3 file
    assertEquals(TEST_FILE_SIZE, object.response().contentLength().intValue());
    assertEquals(tempFile.getRight().toLowerCase(),
        object.response().eTag().toLowerCase().replace("\"", ""));
    // verify latest contents changed
    String latest = S3_CLIENT.getObjectAsBytes(
            GetObjectRequest.builder()
                    .bucket(TEST_BUCKET)
                    .key(LATEST_KEY)
                    .build()).asUtf8String();
    assertEquals(OBJECT_MAPPER.readTree(latest).get("key").asText(), TEST_KEY);
  }

  private Pair<Path, String> generateTempFile() throws IOException, NoSuchAlgorithmException {
    byte[] contents = new byte[TEST_FILE_SIZE];
    new Random().nextBytes(contents);
    final Path tempFile = Files.createTempFile("dice-where", "downloader");
    Files.write(tempFile, contents);
    MessageDigest md = MessageDigest.getInstance("MD5");
    String hex = (new HexBinaryAdapter()).marshal(md.digest(contents));
    return Pair.of(tempFile, hex);
  }
}