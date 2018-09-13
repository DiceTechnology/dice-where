package technology.dice.dicewhere.api.exceptions;

public class DuplicateProviderException extends IllegalStateException {
  public DuplicateProviderException(String message) {
    super(message);
  }
}
