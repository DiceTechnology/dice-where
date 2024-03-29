/*
 * Copyright (C) 2018 - present by Dice Technology Ltd.
 *
 * Please see distribution for license.
 */

package technology.dice.dicewhere.provider.dbip;

import technology.dice.dicewhere.provider.ProviderKey;

public final class DbIpProviderKey extends ProviderKey {

  private static final DbIpProviderKey instance = new DbIpProviderKey();

  public DbIpProviderKey() {
    super("DbIp");
  }

  public static DbIpProviderKey of() {
    return instance;
  }
}
