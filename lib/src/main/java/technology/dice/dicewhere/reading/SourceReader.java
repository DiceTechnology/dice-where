package technology.dice.dicewhere.reading;

import technology.dice.dicewhere.building.DatabaseBuilderListener;
import technology.dice.dicewhere.building.IPDatabase;
import technology.dice.dicewhere.lineprocessing.LineProcessorListener;
import technology.dice.dicewhere.provider.ProviderKey;

public interface SourceReader {
  IPDatabase read(
      boolean retainOriginalLine,
      LineReaderListener readerListener,
      LineProcessorListener processListener,
      DatabaseBuilderListener buildingListener,
      int workersCount);

  ProviderKey provider();
}
