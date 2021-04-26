package technology.dice.dicewhere.downloader.picocli;

import java.io.InputStream;
import java.util.Scanner;
import picocli.CommandLine.IVersionProvider;

public class ResourceVersionProvider implements IVersionProvider {

  @Override
  public String[] getVersion() throws Exception {
    try (InputStream is = ResourceVersionProvider.class.getResourceAsStream("/version")) {
      if (is == null) {
        return new String[] {"Unknown version"};
      }
      final Scanner scanner = new Scanner(is).useDelimiter("\\A");
      String version = scanner.next();
      return new String[] {version};
    }
  }
}
