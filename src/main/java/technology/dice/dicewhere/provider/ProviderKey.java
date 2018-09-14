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
    if (o == null || getClass() != o.getClass()) {
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
