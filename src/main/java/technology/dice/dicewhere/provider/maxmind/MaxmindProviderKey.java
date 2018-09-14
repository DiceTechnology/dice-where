package technology.dice.dicewhere.provider.maxmind;

import technology.dice.dicewhere.provider.ProviderKey;

public class MaxmindProviderKey extends ProviderKey {

  private static final MaxmindProviderKey instance = new MaxmindProviderKey();

  public MaxmindProviderKey() {
    super("Maxmind");
  }

  public static MaxmindProviderKey of() {
    return instance;
  }
}
