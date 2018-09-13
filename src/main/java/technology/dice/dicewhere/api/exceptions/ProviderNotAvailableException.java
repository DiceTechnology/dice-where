package technology.dice.dicewhere.api.exceptions;

import technology.dice.dicewhere.parsing.provider.DatabaseProvider;

public class ProviderNotAvailableException extends IllegalArgumentException {
	private final DatabaseProvider provider;

	public ProviderNotAvailableException(String message, DatabaseProvider provider) {
		super(message);
		this.provider = provider;
	}
}
