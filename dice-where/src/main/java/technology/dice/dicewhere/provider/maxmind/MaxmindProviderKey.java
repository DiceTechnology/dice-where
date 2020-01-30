/*
 * Copyright (C) 2018 - present by Dice Technology Ltd.
 *
 * Please see distribution for license.
 */

package technology.dice.dicewhere.provider.maxmind;

import technology.dice.dicewhere.provider.ProviderKey;

public final class MaxmindProviderKey extends ProviderKey {

  private static final MaxmindProviderKey instance = new MaxmindProviderKey();

  public MaxmindProviderKey() {
    super("Maxmind");
  }

  public static MaxmindProviderKey of() {
    return instance;
  }
}
