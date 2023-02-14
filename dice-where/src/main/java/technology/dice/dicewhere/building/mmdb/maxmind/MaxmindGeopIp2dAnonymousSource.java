package technology.dice.dicewhere.building.mmdb.maxmind;

import java.nio.file.Path;
import technology.dice.dicewhere.building.mmdb.AnonymousResult;
import technology.dice.dicewhere.building.mmdb.MmdbAnonymousSource;

public class MaxmindGeopIp2dAnonymousSource extends MmdbAnonymousSource {

  /**
   * Builds a maxmind anonymous database using an mmdb source
   *
   * @param path to the dataset file.
   */
  public MaxmindGeopIp2dAnonymousSource(Path path) {
    super(path);
  }

  @Override
  public Class<? extends AnonymousResult> anonymousResult() {
    return MaxmindAnonymousResult.class;
  }
}
