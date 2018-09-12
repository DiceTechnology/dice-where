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

	public CompletionStage<Optional<IPInformation>> resolveAsync(IP ip, DatabaseProvider provider) {
		return CompletableFuture.supplyAsync(
				() -> this.databases.get(provider).get(ip)
		);
	}

	public CompletionStage<Optional<IPInformation>> resolveAsync(IP ip, DatabaseProvider provider, ExecutorService executorService) {
		return CompletableFuture.supplyAsync(
				() -> this.databases.get(provider).get(ip)
				, executorService);
	}

	public Optional<IPInformation> resolve(IP ip, DatabaseProvider provider) {
		if (!this.databases.containsKey(provider)) {
			throw new ProviderNotAvailableException(String.format("Provider %s not available", provider.name()), provider);
		}
		return this.databases.get(provider).get(ip);
	}

	public CompletionStage<Optional<IPInformation>> resolveAsync(String ip, DatabaseProvider provider) throws UnknownHostException {
		return this.resolveAsync(new IP(InetAddress.getByName(ip)), provider);
	}

	public CompletionStage<Optional<IPInformation>> resolveAsync(String ip, DatabaseProvider provider, ExecutorService executorService) throws UnknownHostException {
		return this.resolveAsync(new IP(InetAddress.getByName(ip)), provider, executorService);
	}

	public Optional<IPInformation> resolve(String ip, DatabaseProvider provider) throws UnknownHostException {
		return this.resolve(new IP(InetAddress.getByName(ip)), provider);
	}

	public Map<DatabaseProvider, CompletionStage<Optional<IPInformation>>> resolveAsync(IP ip) {
		Map<DatabaseProvider, CompletionStage<Optional<IPInformation>>> resolution = this.databases
				.entrySet()
				.stream()
				.collect(Collectors.toMap(
						Map.Entry::getKey,
						databaseProviderIPDatabaseEntry -> resolveAsync(ip, databaseProviderIPDatabaseEntry.getKey())));

		return resolution;
	}

	public Map<DatabaseProvider, Optional<IPInformation>> resolve(String ip) throws UnknownHostException {
		return resolve(new IP(InetAddress.getByName(ip)));
	}

	public Map<DatabaseProvider, CompletionStage<Optional<IPInformation>>> resolveAsync(String ip) throws UnknownHostException {
		return resolveAsync(new IP(InetAddress.getByName(ip)));
	}

	public Map<DatabaseProvider, CompletionStage<Optional<IPInformation>>> resolveAsync(String ip, ExecutorService executorService) throws UnknownHostException {
		return resolveAsync(new IP(InetAddress.getByName(ip)), executorService);
	}

	public Map<DatabaseProvider, CompletionStage<Optional<IPInformation>>> resolveAsync(IP ip, ExecutorService executorService) {
		Map<DatabaseProvider, CompletionStage<Optional<IPInformation>>> resolution = this.databases
				.entrySet()
				.stream()
				.collect(Collectors.toMap(
						Map.Entry::getKey,
						databaseProviderIPDatabaseEntry -> resolveAsync(ip, databaseProviderIPDatabaseEntry.getKey(), executorService)));

		return resolution;
	}

	public Map<DatabaseProvider, Optional<IPInformation>> resolve(IP ip) {
		Map<DatabaseProvider, Optional<IPInformation>> resolution = this.databases
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
			this.providers = new HashMap<>();
		}

		public Builder withProvider(LineReader lineReader) {
			if (this.providers.containsKey(lineReader.provider())) {
				throw new DuplicateProviderException(String.format("Provider %s has already been added", lineReader.provider().name()));
			}
			this.providers.put(lineReader.provider(), lineReader);
			return this;
		}

		public Builder retainOriginalLine(boolean retain) {
			this.retainOriginalLine = retain;
			return this;
		}

		public Builder withReaderListener(LineReaderListener readerListener) {
			this.readerListener = Objects.requireNonNull(readerListener);
			return this;
		}

		public Builder withProcessorListener(LineProcessorListener processorListener) {
			this.processorListener = Objects.requireNonNull(processorListener);
			return this;
		}

		public Builder withBuilderListener(DatabaseBuilderListener builderListener) {
			this.builderListener = Objects.requireNonNull(builderListener);
			return this;
		}

		public IPResolver build() throws IOException {
			this.checkSanity();
			Map<DatabaseProvider, IPDatabase> databases = new HashMap<>(this.providers.size());
			for (LineReader reader : this.providers.values()) {
				databases.put(reader.provider(), reader.read(this.retainOriginalLine, this.readerListener, this.processorListener, this.builderListener));
			}
			return new IPResolver(databases);
		}

		private void checkSanity() {
			if (this.providers.size() <= 0) {
				throw new NoProvidersException("Must build with at least one provider");
			}
		}
	}
}
