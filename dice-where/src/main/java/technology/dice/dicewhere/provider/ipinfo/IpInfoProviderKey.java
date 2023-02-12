/*
 * Copyright (C) 2018 - present by Dice Technology Ltd.
 *
 * Please see distribution for license.
 */

package technology.dice.dicewhere.provider.ipinfo;

import technology.dice.dicewhere.provider.ProviderKey;

public final class IpInfoProviderKey extends ProviderKey {

  private static final IpInfoProviderKey instance = new IpInfoProviderKey();

  public IpInfoProviderKey() {
    super("IpInfo");
  }

  public static IpInfoProviderKey of() {
    return instance;
  }
}
