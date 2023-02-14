package technology.dice.dicewhere.api;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.profile.ClassloaderProfiler;
import org.openjdk.jmh.profile.GCProfiler;
import org.openjdk.jmh.profile.HotspotClassloadingProfiler;
import org.openjdk.jmh.profile.HotspotMemoryProfiler;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import technology.dice.dicewhere.api.api.IPResolver;
import technology.dice.dicewhere.provider.ProviderKey;
import technology.dice.dicewhere.provider.dbip.reading.DbIpLineReader;
import technology.dice.dicewhere.provider.dbip.reading.DbIpLocationAndIspLineReader;
import technology.dice.dicewhere.provider.maxmind.MaxmindProviderKey;
import technology.dice.dicewhere.provider.maxmind.reading.MaxmindDbReader;
import technology.dice.dicewhere.reading.LineReaderListener;
import technology.dice.dicewhere.reading.RawLine;

import java.io.IOException;
import java.net.UnknownHostException;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

@State(Scope.Thread)
@Fork(value = 1, jvmArgsAppend = "-Djmh.stack.lines=3")
@OutputTimeUnit(TimeUnit.MILLISECONDS)
public class IPResolverBenchmark {

  private static final String IPV4 = "192.168.4.5";
  private static final String IPV6 = "0:0:0:0:0:ffff:c0a8:405";

  private static final String MAXMIND_RESOURCES_FOLDER =
      "/Users/zorg/Downloads/where/GeoIP2-City-CSV_20180911";
  private static final String RESOURCES_FOLDER = "/Users/zorg/Downloads/where";

  private IPResolver resolver;

  public static void main(String[] args) throws RunnerException {

    Options opt =
        new OptionsBuilder()
            .include(IPResolverBenchmark.class.getSimpleName())
            .addProfiler(GCProfiler.class)
            .addProfiler(ClassloaderProfiler.class)
            .addProfiler(HotspotMemoryProfiler.class)
            .addProfiler(HotspotClassloadingProfiler.class)
            .addProfiler(MaxMemoryProfiler.class)
            .forks(1)
            .build();

    new Runner(opt).run();
  }

  @Benchmark
  @BenchmarkMode(Mode.SampleTime)
  @Warmup(iterations = 5)
  @Measurement(iterations = 5, time = 5, timeUnit = TimeUnit.MILLISECONDS)
  public void testIPV4() throws UnknownHostException {
    for (int i = 0; i < 4; ++i) {
      for (int b = 0; b < 255; ++b) {
        resolver.resolve("192.168." + i + "." + b, MaxmindProviderKey.of());
      }
    }
  }

  @Benchmark
  @BenchmarkMode(Mode.SampleTime)
  @Warmup(iterations = 1)
  @Measurement(iterations = 5, time = 100, timeUnit = TimeUnit.MILLISECONDS)
  public void testIPV6() throws UnknownHostException {
    for (int i = 0; i < 1000; ++i) {
      resolver.resolve(IPV6, MaxmindProviderKey.of());
    }
  }

  @TearDown
  public void tearDown() {}

  @Setup
  public void setUp() throws IOException {
    MaxmindDbReader maxmindDbReader =
        new MaxmindDbReader(
            Paths.get(MAXMIND_RESOURCES_FOLDER + "/GeoIP2-City-Locations-en.csv"),
            Paths.get(MAXMIND_RESOURCES_FOLDER + "/GeoIP2-City-Blocks-IPv4.csv"),
            Paths.get(MAXMIND_RESOURCES_FOLDER + "/GeoIP2-City-Blocks-IPv6.csv"));

    DbIpLineReader dbIpLineReader =
        new DbIpLocationAndIspLineReader(Paths.get(RESOURCES_FOLDER + "/dbip-full-2018-09.csv"));

    LineReaderListener lineReaderListener =
        new LineReaderListener() {

          @Override
          public void lineRead(ProviderKey provider, RawLine rawLine, long elapsedMillis) {
            if (rawLine.getLineNumber() % 100000 == 0) {
              System.out.println(
                  Thread.currentThread().getName()
                      + " ##### Read "
                      + rawLine.getLineNumber()
                      + " records so far in + "
                      + elapsedMillis / 1e3
                      + " seconds.");
            }
          }

          @Override
          public void finished(ProviderKey provider, long linesProcessed, long elapsedMillis) {
            System.out.println(
                "Finished processing "
                    + linesProcessed
                    + " lines in "
                    + elapsedMillis / 1e3
                    + " seconds");
          }
        };

    IPResolver.Builder resolverBuilder =
        new IPResolver.Builder()
            .withProvider(maxmindDbReader)
            .withProvider(dbIpLineReader)
            .withReaderListener(lineReaderListener);

    resolver = resolverBuilder.build();
  }
}
