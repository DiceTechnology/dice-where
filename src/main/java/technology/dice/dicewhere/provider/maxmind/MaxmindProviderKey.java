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

  @Override
  public int hashCode() {
    return super.hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    return super.equals(obj);
  }
}
