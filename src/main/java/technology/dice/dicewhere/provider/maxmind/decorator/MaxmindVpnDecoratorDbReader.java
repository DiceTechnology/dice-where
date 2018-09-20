package technology.dice.dicewhere.provider.maxmind.decorator;

import com.google.common.base.Splitter;
import inet.ipaddr.IPAddressString;
import technology.dice.dicewhere.api.api.IP;
import technology.dice.dicewhere.decorator.DecoratorDbReader;
import technology.dice.dicewhere.decorator.VpnDecoratorInformation;
import technology.dice.dicewhere.utils.StringUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Iterator;
import java.util.Optional;

public class MaxmindVpnDecoratorDbReader extends DecoratorDbReader<VpnDecoratorInformation> {
  private final Splitter splitter = Splitter.on(",");

  private final BufferedReader ipV4AnonymousDatabase;
  private boolean ipV4DbExhausted = false;
  private final BufferedReader ipV6AnonymousDatabase;

  public MaxmindVpnDecoratorDbReader(
      BufferedReader ipV4AnonymousDatabase, BufferedReader ipV6AnonymousDatabase)
      throws IOException {

    this.ipV4AnonymousDatabase = ipV4AnonymousDatabase;
    this.ipV6AnonymousDatabase = ipV6AnonymousDatabase;
    this.ipV4AnonymousDatabase.readLine(); // first line is header
    this.ipV6AnonymousDatabase.readLine(); // first line is header
    this.readNextLine();
  }

  private Optional<String> readLine() throws IOException {
    if (!ipV4DbExhausted) {
      Optional<String> line = Optional.ofNullable(ipV4AnonymousDatabase.readLine());
      if (!line.isPresent()) {
        ipV4DbExhausted = true;
      } else {
        return line;
      }
    }
    return Optional.ofNullable(ipV6AnonymousDatabase.readLine());
  }

  @Override
  protected void readNextLine() {
    try {
      if (ipV4AnonymousDatabase != null && ipV6AnonymousDatabase != null) {
        while (true) {
          Optional<String> readLine = readLine();
          if (readLine.isPresent()) {
            Optional<VpnDecoratorInformation> parsedLine = readLine.flatMap(this::parseDbLine);
            this.setLastFetched(parsedLine.orElse(null));
            if (parsedLine.isPresent()) {
              break;
            } // else: the line read from DB couldn't be parsed or didn't match the criteria
          } else {
            // no more lines to read from the DB
            break;
          }
        }
      }
    } catch (IOException e) {
      this.setLastFetched(null);
    }
  }

  @Override
  protected Optional<VpnDecoratorInformation> parseDbLine(String line) {
    Iterator<String> fieldsIterator = splitter.split(line).iterator();
    String range = StringUtils.removeQuotes(fieldsIterator.next());
    IPAddressString rangeString = new IPAddressString(range);
    if (rangeString.getAddress() == null) {
      // is this the best approach or should we throw an exception?
      return Optional.empty();
    }
    IP l = new IP(rangeString.getAddress().getLower().getBytes());
    IP u = new IP(rangeString.getAddress().toMaxHost().getBytes());
    fieldsIterator.next(); // anonymous
    boolean isVpn = "1".equalsIgnoreCase(fieldsIterator.next());
    if (isVpn) {
      return Optional.of(new VpnDecoratorInformation(l, u));
    } else {
      return Optional.empty();
    }
  }
}
