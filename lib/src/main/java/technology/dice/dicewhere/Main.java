package technology.dice.dicewhere;

import java.io.IOException;
import java.net.InetAddress;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Optional;
import technology.dice.dicewhere.api.api.IP;
import technology.dice.dicewhere.api.api.IPResolver;
import technology.dice.dicewhere.api.api.IPResolver.Builder;
import technology.dice.dicewhere.api.api.IpInformation;
import technology.dice.dicewhere.provider.ProviderKey;
import technology.dice.dicewhere.reading.maxmind.MaxmindNativeSource;

public class Main {
  public static void main(String[] args) throws IOException {
    final IPResolver build =
        new Builder()
            .withProvider(
                new MaxmindNativeSource(
                    Paths.get("/Users/gluiz/Downloads/GeoIP2-City_20200121/GeoIP2-City.mmdb"),
                    Paths.get(
                        "/Users/gluiz/Downloads/GeoIP2-Anonymous-IP_20200124/GeoIP2-Anonymous-IP.mmdb")))
            .build();
    final Map<ProviderKey, Optional<IpInformation>> resolve =
        build.resolve(new IP(InetAddress.getByName("82.102.20.51")));

    int a = 2;
  }
}
