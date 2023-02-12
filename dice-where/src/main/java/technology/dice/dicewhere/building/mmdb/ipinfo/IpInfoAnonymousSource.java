package technology.dice.dicewhere.building.mmdb.ipinfo;

import java.io.IOException;
import java.nio.file.Path;
import technology.dice.dicewhere.building.mmdb.AnonymousResult;
import technology.dice.dicewhere.building.mmdb.MmdbAnonymousSource;

public class IpInfoAnonymousSource extends MmdbAnonymousSource {

  /**
   * Builds a ipinfo privacy database using an mmdb source
   *
   * @param path to the dataset file.
   */
  public IpInfoAnonymousSource(Path path) throws IOException {
    super(path);
  }

  @Override
  public Class<? extends AnonymousResult> anonymousResult() {
    return IpInfoAnonymousResult.class;
  }
}
