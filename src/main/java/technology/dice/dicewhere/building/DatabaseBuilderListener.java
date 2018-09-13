package technology.dice.dicewhere.building;

import technology.dice.dicewhere.lineprocessing.SerializedLine;
import technology.dice.dicewhere.parsing.provider.DatabaseProvider;

public interface DatabaseBuilderListener {
  default void lineOutOfOrder(
      DatabaseProvider provider, SerializedLine serializedLine, Exception e) {
    throw new RuntimeException(e);
  }

  default void builderInterrupted(DatabaseProvider provider, InterruptedException e) {
    throw new RuntimeException(e);
  }

  default void lineAdded(DatabaseProvider provider, SerializedLine serializedLine) {}
}
