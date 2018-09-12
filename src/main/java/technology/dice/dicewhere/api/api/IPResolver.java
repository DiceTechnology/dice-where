package technology.dice.dicewhere.api.api;

import technology.dice.dicewhere.api.exceptions.DuplicateProviderException;
import technology.dice.dicewhere.api.exceptions.NoProvidersException;
import technology.dice.dicewhere.api.exceptions.ProviderNotAvailableException;
import technology.dice.dicewhere.building.DatabaseBuilderListener;
import technology.dice.dicewhere.building.IPDatabase;
import technology.dice.dicewhere.lineprocessing.LineProcessorListener;
import technology.dice.dicewhere.parsing.provider.DatabaseProvider;
import technology.dice.dicewhere.reading.LineReader;
import technology.dice.dicewhere.reading.LineReaderListener;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

public class IPResolver {
	private final Map<DatabaseProvider, IPDatabase> databases;

	private IPResolver(Map<DatabaseProvider, IPDatabase> databases) {
		this.databases = databases;
	}

	public CompletionStage<Optional<IpInformation>> resolveAsync(IP ip, DatabaseProvider provider) {
		return CompletableFuture.supplyAsync(
				() -> databases.get(provider).get(ip)
		);
	}

	public CompletionStage<Optional<IpInformation>> resolveAsync(IP ip, DatabaseProvider provider, ExecutorService executorService) {
		return CompletableFuture.supplyAsync(
				() -> databases.get(provider).get(ip)
				, executorService);
	}

	public Optional<IpInformation> resolve(IP ip, DatabaseProvider provider) {
		if (!databases.containsKey(provider)) {
			throw new ProviderNotAvailableException(String.format("Provider %s not available", provider.name()), provider);
		}
		return databases.get(provider).get(ip);
	}

	public CompletionStage<Optional<IpInformation>> resolveAsync(String ip, DatabaseProvider provider) throws UnknownHostException {
		return resolveAsync(new IP(InetAddress.getByName(ip)), provider);
	}

	public CompletionStage<Optional<IpInformation>> resolveAsync(String ip, DatabaseProvider provider, ExecutorService executorService) throws UnknownHostException {
		return resolveAsync(new IP(InetAddress.getByName(ip)), provider, executorService);
	}

	public Optional<IpInformation> resolve(String ip, DatabaseProvider provider) throws UnknownHostException {
		return resolve(new IP(InetAddress.getByName(ip)), provider);
	}

	public Map<DatabaseProvider, CompletionStage<Optional<IpInformation>>> resolveAsync(IP ip) {
		Map<DatabaseProvider, CompletionStage<Optional<IpInformation>>> resolution = databases
				.entrySet()
				.stream()
				.collect(Collectors.toMap(
						Map.Entry::getKey,
						databaseProviderIPDatabaseEntry -> resolveAsync(ip, databaseProviderIPDatabaseEntry.getKey())));

		return resolution;
	}

	public Map<DatabaseProvider, Optional<IpInformation>> resolve(String ip) throws UnknownHostException {
		return resolve(new IP(InetAddress.getByName(ip)));
	}

	public Map<DatabaseProvider, CompletionStage<Optional<IpInformation>>> resolveAsync(String ip) throws UnknownHostException {
		return resolveAsync(new IP(InetAddress.getByName(ip)));
	}

	public Map<DatabaseProvider, CompletionStage<Optional<IpInformation>>> resolveAsync(String ip, ExecutorService executorService) throws UnknownHostException {
		return resolveAsync(new IP(InetAddress.getByName(ip)), executorService);
	}

	public Map<DatabaseProvider, CompletionStage<Optional<IpInformation>>> resolveAsync(IP ip, ExecutorService executorService) {
		Map<DatabaseProvider, CompletionStage<Optional<IpInformation>>> resolution = databases
				.entrySet()
				.stream()
				.collect(Collectors.toMap(
						Map.Entry::getKey,
						databaseProviderIPDatabaseEntry -> resolveAsync(ip, databaseProviderIPDatabaseEntry.getKey(), executorService)));

		return resolution;
	}

	public Map<DatabaseProvider, Optional<IpInformation>> resolve(IP ip) {
		Map<DatabaseProvider, Optional<IpInformation>> resolution = databases
				.entrySet()
				.stream()
				.collect(Collectors.toMap(
						Map.Entry::getKey,
						databaseProviderIPDatabaseEntry -> resolve(ip, databaseProviderIPDatabaseEntry.getKey())));

		return resolution;
	}

	public static class Builder {
		private final Map<DatabaseProvider, LineReader> providers;
		private boolean retainOriginalLine = false;
		private LineReaderListener readerListener = new LineReaderListener() {
		};
		private LineProcessorListener processorListener = new LineProcessorListener() {
		};
		private DatabaseBuilderListener builderListener = new DatabaseBuilderListener() {
		};

		public Builder() {
			providers = new HashMap<>();
		}

		public Builder withProvider(LineReader lineReader) {
			if (providers.containsKey(lineReader.provider())) {
				throw new DuplicateProviderException(String.format("Provider %s has already been added", lineReader.provider().name()));
			}
			providers.put(lineReader.provider(), lineReader);
			return this;
		}

		public Builder retainOriginalLine(boolean retain) {
			retainOriginalLine = retain;
			return this;
		}

		public Builder withReaderListener(LineReaderListener readerListener) {
			readerListener = Objects.requireNonNull(readerListener);
			return this;
		}

		public Builder withProcessorListener(LineProcessorListener processorListener) {
			processorListener = Objects.requireNonNull(processorListener);
			return this;
		}

		public Builder withBuilderListener(DatabaseBuilderListener builderListener) {
			builderListener = Objects.requireNonNull(builderListener);
			return this;
		}

		public IPResolver build() throws IOException {
			checkSanity();
			Map<DatabaseProvider, IPDatabase> databases = new HashMap<>(providers.size());
			for (LineReader reader : providers.values()) {
				databases.put(reader.provider(), reader.read(retainOriginalLine, readerListener, processorListener, builderListener));
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
