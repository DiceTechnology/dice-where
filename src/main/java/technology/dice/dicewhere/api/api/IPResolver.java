package technology.dice.dicewhere.api.api;

import com.google.common.collect.ImmutableMap;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import technology.dice.dicewhere.api.exceptions.DuplicateProviderException;
import technology.dice.dicewhere.api.exceptions.NoProvidersException;
import technology.dice.dicewhere.api.exceptions.ProviderNotAvailableException;
import technology.dice.dicewhere.building.DatabaseBuilderListener;
import technology.dice.dicewhere.building.IPDatabase;
import technology.dice.dicewhere.lineprocessing.LineProcessorListener;
import technology.dice.dicewhere.provider.ProviderKey;
import technology.dice.dicewhere.reading.LineReader;
import technology.dice.dicewhere.reading.LineReaderListener;

public class IPResolver {
  private final Map<ProviderKey, IPDatabase> databases;

  private IPResolver(Map<ProviderKey, IPDatabase> databases) {
    this.databases = databases;
  }

  public CompletionStage<Optional<IpInformation>> resolveAsync(
      @Nonnull IP ip, @Nonnull ProviderKey provider) {
    return CompletableFuture.supplyAsync(
        () -> databases.get(Objects.requireNonNull(provider)).get(Objects.requireNonNull(ip)));
  }

  public CompletionStage<Optional<IpInformation>> resolveAsync(
      @Nonnull IP ip, @Nonnull ProviderKey provider, @Nonnull ExecutorService executorService) {
    return CompletableFuture.supplyAsync(
        () -> databases.get(Objects.requireNonNull(provider)).get(Objects.requireNonNull(ip)),
        Objects.requireNonNull(executorService));
  }

  public Optional<IpInformation> resolve(@Nonnull IP ip, @Nonnull ProviderKey provider) {
    if (!databases.containsKey(Objects.requireNonNull(provider))) {
      throw new ProviderNotAvailableException(
          String.format("Provider %s not available", provider.name()), provider);
    }
    return databases.get(Objects.requireNonNull(provider)).get(Objects.requireNonNull(ip));
  }

  public CompletionStage<Optional<IpInformation>> resolveAsync(
      @Nonnull String ip, @Nonnull ProviderKey provider) throws UnknownHostException {
    return resolveAsync(
        new IP(InetAddress.getByName(Objects.requireNonNull(ip))),
        Objects.requireNonNull(provider));
  }

  public CompletionStage<Optional<IpInformation>> resolveAsync(
      @Nonnull String ip, @Nonnull ProviderKey provider, @Nonnull ExecutorService executorService)
      throws UnknownHostException {
    return resolveAsync(
        new IP(InetAddress.getByName(Objects.requireNonNull(ip))),
        Objects.requireNonNull(provider),
        Objects.requireNonNull(executorService));
  }

  public Optional<IpInformation> resolve(@Nonnull String ip, @Nonnull ProviderKey provider)
      throws UnknownHostException {
    return resolve(
        new IP(InetAddress.getByName(Objects.requireNonNull(ip))),
        Objects.requireNonNull(provider));
  }

  public CompletionStage<Map<ProviderKey, Optional<IpInformation>>> resolveAsync(@Nonnull IP ip) {

    Map<ProviderKey, CompletableFuture<Optional<IpInformation>>> resolution =
        databases
            .entrySet()
            .stream()
            .collect(
                ImmutableMap.toImmutableMap(
                    Map.Entry::getKey,
                    entry ->
                        resolveAsync(Objects.requireNonNull(ip), entry.getKey())
                            .toCompletableFuture()));

    return CompletableFuture.allOf(resolution.values().toArray(new CompletableFuture<?>[0]))
        .thenApply(
            res ->
                resolution
                    .entrySet()
                    .stream()
                    .collect(
                        ImmutableMap.toImmutableMap(
                            Map.Entry::getKey, entry -> entry.getValue().join())));
  }

  public Map<ProviderKey, Optional<IpInformation>> resolve(@Nonnull String ip)
      throws UnknownHostException {
    return resolve(new IP(InetAddress.getByName(Objects.requireNonNull(ip))));
  }

  public CompletionStage<Map<ProviderKey, Optional<IpInformation>>> resolveAsync(@Nonnull String ip)
      throws UnknownHostException {
    return resolveAsync(new IP(InetAddress.getByName(Objects.requireNonNull(ip))));
  }

  public Map<ProviderKey, CompletionStage<Optional<IpInformation>>> resolveAsync(
      @Nonnull String ip, @Nonnull ExecutorService executorService) throws UnknownHostException {
    return resolveAsync(
        new IP(InetAddress.getByName(Objects.requireNonNull(ip))),
        Objects.requireNonNull(executorService));
  }

  public Map<ProviderKey, CompletionStage<Optional<IpInformation>>> resolveAsync(
      @Nonnull IP ip, @Nonnull ExecutorService executorService) {
    Map<ProviderKey, CompletionStage<Optional<IpInformation>>> resolution =
        databases
            .entrySet()
            .stream()
            .collect(
                Collectors.toMap(
                    Map.Entry::getKey,
                    databaseProviderIPDatabaseEntry ->
                        resolveAsync(
                            Objects.requireNonNull(ip),
                            databaseProviderIPDatabaseEntry.getKey(),
                            Objects.requireNonNull(executorService))));

    return resolution;
  }

  public Map<ProviderKey, Optional<IpInformation>> resolve(@Nonnull IP ip) {
    Map<ProviderKey, Optional<IpInformation>> resolution =
        databases
            .entrySet()
            .stream()
            .collect(
                Collectors.toMap(
                    Map.Entry::getKey,
                    databaseProviderIPDatabaseEntry ->
                        resolve(
                            Objects.requireNonNull(ip), databaseProviderIPDatabaseEntry.getKey())));

    return resolution;
  }

  public static class Builder {
    private final Map<ProviderKey, LineReader> providers;
    private boolean retainOriginalLine = false;
    private LineReaderListener readerListener = new LineReaderListener() {};
    private LineProcessorListener processorListener = new LineProcessorListener() {};
    private DatabaseBuilderListener builderListener = new DatabaseBuilderListener() {};

    public Builder() {
      providers = new HashMap<>();
    }

    public Builder withProvider(@Nonnull LineReader lineReader) {
      if (providers.containsKey(Objects.requireNonNull(lineReader).provider())) {
        throw new DuplicateProviderException(
            String.format("Provider %s has already been added", lineReader.provider().name()));
      }
      providers.put(lineReader.provider(), lineReader);
      return this;
    }

    public Builder retainOriginalLine(boolean retain) {
      retainOriginalLine = retain;
      return this;
    }

    public Builder withReaderListener(@Nonnull LineReaderListener readerListener) {
      this.readerListener = Objects.requireNonNull(readerListener);
      return this;
    }

    public Builder withProcessorListener(@Nonnull LineProcessorListener processorListener) {
      this.processorListener = Objects.requireNonNull(processorListener);
      return this;
    }

    public Builder withBuilderListener(@Nonnull DatabaseBuilderListener builderListener) {
      this.builderListener = Objects.requireNonNull(builderListener);
      return this;
    }

    public IPResolver build() throws IOException {
      checkSanity();
      Map<ProviderKey, IPDatabase> databases = new HashMap<>(providers.size());
      for (LineReader reader : providers.values()) {
        databases.put(
            reader.provider(),
            reader.read(retainOriginalLine, readerListener, processorListener, builderListener));
      }
      return new IPResolver(databases);
    }

    private void checkSanity() {
      if (providers.size() <= 0) {
        throw new NoProvidersException("Must build with at least one provider");
      }
    }
  }
}
