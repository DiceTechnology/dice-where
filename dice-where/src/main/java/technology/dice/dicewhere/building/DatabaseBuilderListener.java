/*
 * Copyright (C) 2018 - present by Dice Technology Ltd.
 *
 * Please see distribution for license.
 */

package technology.dice.dicewhere.building;

import technology.dice.dicewhere.lineprocessing.SerializedLine;
import technology.dice.dicewhere.provider.ProviderKey;

public interface DatabaseBuilderListener {
  default void lineOutOfOrder(ProviderKey provider, SerializedLine serializedLine, Exception e) {
    throw new RuntimeException(e);
  }

  default void builderInterrupted(ProviderKey provider, InterruptedException e) {
    throw new RuntimeException(e);
  }

  default void lineAdded(ProviderKey provider, SerializedLine serializedLine) {}
}
