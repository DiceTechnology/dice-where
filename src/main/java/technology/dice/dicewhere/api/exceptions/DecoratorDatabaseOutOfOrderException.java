/*
 * Copyright (C) 2018 - present by Dice Technology Ltd.
 *
 * Please see distribution for license.
 */

package technology.dice.dicewhere.api.exceptions;

public class DecoratorDatabaseOutOfOrderException extends RuntimeException {

  public DecoratorDatabaseOutOfOrderException(String message, Throwable cause) {
    super(message, cause);
  }

  public DecoratorDatabaseOutOfOrderException(String message) {
    super(message);
  }
}
