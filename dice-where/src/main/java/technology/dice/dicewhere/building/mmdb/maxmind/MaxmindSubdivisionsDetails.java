package technology.dice.dicewhere.building.mmdb.maxmind;

import com.maxmind.db.MaxMindDbConstructor;
import com.maxmind.db.MaxMindDbParameter;
import java.util.Map;
import java.util.Optional;

public class MaxmindSubdivisionsDetails {

  private final Map<String, String> names;

  @MaxMindDbConstructor
  public MaxmindSubdivisionsDetails(@MaxMindDbParameter(name = "names") Map<String, String> names) {
    this.names = names;
  }

  public String name(String locale) {
    return Optional.of(names.get(locale)).orElse(null);
  }
}
