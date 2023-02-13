package technology.dice.dicewhere.building.mmdb;

import com.maxmind.db.CHMCache;
import com.maxmind.db.Reader;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.nio.file.Path;
import technology.dice.dicewhere.api.api.IP;
import technology.dice.dicewhere.api.api.IpInformation;

public abstract class MmdbAnonymousSource implements AnonymousSource {

  private final Reader anonymous;

  public MmdbAnonymousSource(Path path) {
    try {
      this.anonymous = new Reader(new File(path.toFile().toURI()), new CHMCache());

    } catch (IOException e) {
      throw new RuntimeException("Error accessing file " + path.toFile());
    }
  }

  public abstract Class<? extends AnonymousResult> anonymousResult();

  @Override
  public IpInformation withAnonymousInformation(IP ip, IpInformation ipInformation) {
    try {
      final InetAddress inetAddress = InetAddress.getByAddress(ip.getBytes());
      final AnonymousResult anonymousIpResponse = anonymous.get(inetAddress, anonymousResult());
      if (anonymousIpResponse != null) {
        return IpInformation.builder(ipInformation)
            .isHostingProvider(anonymousIpResponse.hostingProvider())
            .isVpn(anonymousIpResponse.vpn())
            .build();
      }
      return ipInformation;
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
