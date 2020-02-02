/*
 * Copyright (C) 2018 - present by Dice Technology Ltd.
 *
 * Please see distribution for license.
 */

package technology.dice.dicewhere.reading;

import technology.dice.dicewhere.provider.ProviderKey;

public interface LineReaderListener {
  default void lineRead(ProviderKey provider, RawLine rawLine, long elapsedMillis) {}

  default void finished(ProviderKey provider, long linesProcessed, long elapsedMillis) {}
}
