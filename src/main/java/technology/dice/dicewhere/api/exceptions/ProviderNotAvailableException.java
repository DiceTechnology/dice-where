package technology.dice.dicewhere.api.exceptions;

import technology.dice.dicewhere.provider.ProviderKey;

public class ProviderNotAvailableException extends IllegalArgumentException {
  private final ProviderKey provider;

  public ProviderNotAvailableException(String message, ProviderKey provider) {
    super(message);
    this.provider = provider;
  }
}
