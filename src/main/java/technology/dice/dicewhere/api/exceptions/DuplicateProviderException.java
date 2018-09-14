/*
 * Copyright (C) 2018 - present by Dice Technology Ltd.
 *
 * Please see distribution for license.
 */

package technology.dice.dicewhere.api.exceptions;

public class DuplicateProviderException extends IllegalStateException {
  public DuplicateProviderException(String message) {
    super(message);
  }
}
