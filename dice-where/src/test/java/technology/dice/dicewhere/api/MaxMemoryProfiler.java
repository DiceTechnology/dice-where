package technology.dice.dicewhere.api;

import org.openjdk.jmh.infra.BenchmarkParams;
import org.openjdk.jmh.infra.IterationParams;
import org.openjdk.jmh.profile.InternalProfiler;
import org.openjdk.jmh.results.AggregationPolicy;
import org.openjdk.jmh.results.IterationResult;
import org.openjdk.jmh.results.Result;
import org.openjdk.jmh.results.ScalarResult;

import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.Collection;

public class MaxMemoryProfiler implements InternalProfiler {

  @Override
  public String getDescription() {
    return "Max memory heap profiler";
  }

  @Override
  public void beforeIteration(BenchmarkParams benchmarkParams, IterationParams iterationParams) {}

  @Override
  public Collection<? extends Result> afterIteration(
      BenchmarkParams benchmarkParams, IterationParams iterationParams, IterationResult result) {

    long heap = ManagementFactory.getMemoryMXBean().getHeapMemoryUsage().getUsed();
    long nonHeap = ManagementFactory.getMemoryMXBean().getNonHeapMemoryUsage().getUsed();

    Collection<ScalarResult> results = new ArrayList<>();
    results.add(new ScalarResult("Current memory usage of the heap", heap, "bytes", AggregationPolicy.MAX));
    results.add(new ScalarResult("Current memory usage of non-heap memory", nonHeap, "bytes", AggregationPolicy.MAX));

    return results;
  }
}
