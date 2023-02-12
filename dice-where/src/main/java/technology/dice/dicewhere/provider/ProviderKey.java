/*
 * Copyright (C) 2018 - present by Dice Technology Ltd.
 *
 * Please see distribution for license.
 */

package technology.dice.dicewhere.provider;

import java.util.Objects;

public abstract class ProviderKey {

  private final String name;

  protected ProviderKey(String name) {
    this.name = name;
  }

  public String name() {
    return name;
  }

  @Override
  public final boolean equals(Object o) {
    if (this == o) {
      return true;
    }

    // TODO GLUIZ
    if (o == null) {
      return false;
    }
    ProviderKey that = (ProviderKey) o;
    return Objects.equals(name, that.name);
  }

  @Override
  public final int hashCode() {
    return Objects.hash(name);
  }
}
