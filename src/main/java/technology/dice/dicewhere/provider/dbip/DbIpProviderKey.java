package technology.dice.dicewhere.provider.dbip;

import technology.dice.dicewhere.provider.ProviderKey;

public class DbIpProviderKey extends ProviderKey {

  private static final DbIpProviderKey instance = new DbIpProviderKey();

  public DbIpProviderKey() {
    super("DbIp");
  }

  public static DbIpProviderKey of() {
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
