package technology.dice.dicewhere.downloader.destination.s3;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertThrows;

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
import java.time.Instant;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;
import javax.xml.bind.annotation.adapters.HexBinaryAdapter;
import junit.framework.TestCase;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpStatus;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.internal.runners.JUnit4ClassRunner;
import org.junit.runner.RunWith;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.containers.localstack.LocalStackContainer.Service;
import org.testcontainers.containers.wait.strategy.HttpWaitStrategy;
import org.testcontainers.shaded.com.google.common.collect.ImmutableMap;
import org.testcontainers.shaded.org.apache.commons.lang3.tuple.Pair;
import org.testcontainers.utility.DockerImageName;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.Delete;
import software.amazon.awssdk.services.s3.model.DeleteBucketRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectsRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.ListObjectsRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsResponse;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.ObjectIdentifier;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.utils.StringInputStream;
import technology.dice.dicewhere.downloader.ObjectMapperInstance;
import technology.dice.dicewhere.downloader.files.FileInfo;
import technology.dice.dicewhere.downloader.source.ipinfosite.IpInfoSiteSource;

@RunWith(JUnit4ClassRunner.class)
public class S3FileAcceptorTest extends TestCase {

  private static final int TEST_FILE_SIZE = 1024 * 1024;
  public static final String TEST_BUCKET = "test-bucket";
  public static final String TEST_KEY = "downloads/test-file";

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

  }

  @Before
  public void before() {
    S3_CLIENT.createBucket(CreateBucketRequest.builder().bucket(TEST_BUCKET).build());
  }

  @After
  public void after() {
    emptyBucket(TEST_BUCKET);
    S3_CLIENT.deleteBucket(DeleteBucketRequest.builder().bucket(TEST_BUCKET).build());
  }

  @Test
  public void corruptedFileEmptyPreexistingSet() throws IOException, NoSuchAlgorithmException {
    Pair<Path, String> tempFile = generateTempFile();
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
    ipInfoSiteSource.produce(
        new S3FileAcceptor(S3_CLIENT, TEST_BUCKET, TEST_KEY, ObjectMapperInstance.INSTANCE,
            Clock.systemUTC()), false);
    assertNotEquals(tempFile.getRight().toLowerCase(), fileInfo.getMd5Checksum().stringFormat());
    assertThrows(NoSuchKeyException.class, () -> S3_CLIENT.getObject(
        GetObjectRequest.builder()
            .bucket(TEST_BUCKET)
            .key(TEST_KEY)
            .build()));
    assertThrows(NoSuchKeyException.class, () -> S3_CLIENT.getObject(
        GetObjectRequest.builder()
            .bucket(TEST_BUCKET)
            .key("downloads/latest")
            .build()));
  }

  @Test
  public void corruptedFilePreexistingSet() throws IOException, NoSuchAlgorithmException {
    Pair<Path, String> preexistingFile = generateTempFile();
    S3_CLIENT.putObject(PutObjectRequest.builder()
        .bucket(TEST_BUCKET)
        .key("downloads/existingFile.zip")
        .build(), preexistingFile.getLeft());
    final Instant originalFileUploadedAt = Instant.now();
    ImmutableMap<String, Object> existingLatest = ImmutableMap.of("key",
        "downloads/existingFile.zip", "uploadedAt", originalFileUploadedAt);
    String existingLatestString = ObjectMapperInstance.INSTANCE.writeValueAsString(existingLatest);
    S3_CLIENT.putObject(PutObjectRequest.builder()
        .bucket(TEST_BUCKET)
        .key("downloads/latest")
        .build(), RequestBody.fromInputStream(new StringInputStream(existingLatestString),
        existingLatestString.length()));

    Pair<Path, String> tempFile = generateTempFile();
    IpInfoSiteSource ipInfoSiteSource = new IpInfoSiteSource(
        new URL("http://localhost:" + wireMockRule.port() + "/data/file.mdb"));
    wireMockRule.stubFor(WireMock.head(UrlPattern.ANY).willReturn(
        aResponse().withStatus(HttpStatus.SC_OK)
            .withHeader("Etag", "bbb")
            .withHeader("Content-Length", Long.toString(TEST_FILE_SIZE))
            .withHeader("Last-Modified", "Thu, 01 Dec 1994 16:00:00 GMT")));
    wireMockRule.stubFor(WireMock.get(UrlPattern.ANY)
        .willReturn(aResponse().withBody(
            IOUtils.toByteArray(new FileInputStream(tempFile.getLeft().toFile())))));

    FileInfo fileInfo = ipInfoSiteSource.fileInfo();
    ipInfoSiteSource.produce(
        new S3FileAcceptor(S3_CLIENT, TEST_BUCKET, TEST_KEY, ObjectMapperInstance.INSTANCE,
            Clock.systemUTC()), false);
    assertNotEquals(tempFile.getRight().toLowerCase(), fileInfo.getMd5Checksum().stringFormat());
    assertThrows(NoSuchKeyException.class, () -> S3_CLIENT.getObject(
        GetObjectRequest.builder()
            .bucket(TEST_BUCKET)
            .key(TEST_KEY)
            .build()));
    ResponseInputStream<GetObjectResponse> latest = S3_CLIENT.getObject(
        GetObjectRequest.builder()
            .bucket(TEST_BUCKET)
            .key("downloads/latest")
            .build());
    Map<String, String> latestInformation = ObjectMapperInstance.INSTANCE.readValue(latest,
        Map.class);

    assertEquals(latestInformation.get("key"), "downloads/existingFile.zip");
    assertEquals(Instant.parse(latestInformation.get("uploadedAt")), originalFileUploadedAt);
  }

  @Test
  public void goodFileEmptyPreexistingSet() throws IOException, NoSuchAlgorithmException {
    Pair<Path, String> tempFile = generateTempFile();
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
    ipInfoSiteSource.produce(
        new S3FileAcceptor(S3_CLIENT, TEST_BUCKET, TEST_KEY, ObjectMapperInstance.INSTANCE,
            Clock.systemUTC()), false);
    assertEquals(fileInfo.getMd5Checksum().stringFormat().toLowerCase(),
        tempFile.getRight().toLowerCase());
    ResponseInputStream<GetObjectResponse> uploadedFile = S3_CLIENT.getObject(
        GetObjectRequest.builder()
            .bucket(TEST_BUCKET)
            .key(TEST_KEY)
            .build());
    ResponseInputStream<GetObjectResponse> latest = S3_CLIENT.getObject(
        GetObjectRequest.builder()
            .bucket(TEST_BUCKET)
            .key("downloads/latest")
            .build());
    Map<String, String> latestInformation = ObjectMapperInstance.INSTANCE.readValue(latest,
        Map.class);

    assertEquals(TEST_FILE_SIZE, uploadedFile.response().contentLength().intValue());
    assertEquals(tempFile.getRight().toLowerCase(),
        uploadedFile.response().eTag().toLowerCase().replace("\"", ""));
    assertEquals(latestInformation.get("key"), "downloads/test-file");
  }

  @Test
  public void goodFilePreexistingSet() throws IOException, NoSuchAlgorithmException {
    Pair<Path, String> preexistingFile = generateTempFile();
    S3_CLIENT.putObject(PutObjectRequest.builder()
        .bucket(TEST_BUCKET)
        .key("downloads/existingFile.zip")
        .build(), preexistingFile.getLeft());
    final Instant originalFileUploadedAt = Instant.now();
    ImmutableMap<String, Object> existingLatest = ImmutableMap.of("key",
        "downloads/existingFile.zip", "uploadedAt", originalFileUploadedAt);
    String existingLatestString = ObjectMapperInstance.INSTANCE.writeValueAsString(existingLatest);
    S3_CLIENT.putObject(PutObjectRequest.builder()
        .bucket(TEST_BUCKET)
        .key("downloads/latest")
        .build(), RequestBody.fromInputStream(new StringInputStream(existingLatestString),
        existingLatestString.length()));

    Pair<Path, String> tempFile = generateTempFile();
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
    ipInfoSiteSource.produce(
        new S3FileAcceptor(S3_CLIENT, TEST_BUCKET, TEST_KEY, ObjectMapperInstance.INSTANCE,
            Clock.systemUTC()), false);
    assertEquals(fileInfo.getMd5Checksum().stringFormat().toLowerCase(),
        tempFile.getRight().toLowerCase());
    ResponseInputStream<GetObjectResponse> uploadedFile = S3_CLIENT.getObject(
        GetObjectRequest.builder()
            .bucket(TEST_BUCKET)
            .key(TEST_KEY)
            .build());
    ResponseInputStream<GetObjectResponse> latest = S3_CLIENT.getObject(
        GetObjectRequest.builder()
            .bucket(TEST_BUCKET)
            .key("downloads/latest")
            .build());
    Map<String, Object> latestInformation = ObjectMapperInstance.INSTANCE.readValue(latest,
        Map.class);

    assertEquals(TEST_FILE_SIZE, uploadedFile.response().contentLength().intValue());
    assertEquals(tempFile.getRight().toLowerCase(),
        uploadedFile.response().eTag().toLowerCase().replace("\"", ""));
    assertEquals(latestInformation.get("key"), "downloads/test-file");
    assertNotEquals(latestInformation.get("uploadedAt"), originalFileUploadedAt);
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

  public static void emptyBucket(String bucketName) {
    ListObjectsResponse listObjectsResponse = S3_CLIENT.listObjects(
        ListObjectsRequest.builder()
            .bucket(bucketName)
            .build());

    if (listObjectsResponse.contents().size() > 0) {
      Delete del = Delete.builder()
          .objects(listObjectsResponse.contents().stream()
              .map(o -> ObjectIdentifier.builder().key(o.key()).build())
              .collect(Collectors.toList()))
          .build();

      DeleteObjectsRequest multiObjectDeleteRequest = DeleteObjectsRequest.builder()
          .bucket(bucketName)
          .delete(del)
          .build();

      S3_CLIENT.deleteObjects(multiObjectDeleteRequest);
    }
  }
}