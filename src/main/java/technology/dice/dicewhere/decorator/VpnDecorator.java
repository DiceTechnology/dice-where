package technology.dice.dicewhere.decorator;

import technology.dice.dicewhere.api.api.IP;
import technology.dice.dicewhere.api.api.IpInformation;

import java.util.Collection;

public class VpnDecorator extends Decorator<VpnDecoratorInformation> {

  public VpnDecorator(
      Collection<DecoratorDbReader<VpnDecoratorInformation>> databaseReaders,
      DecorationStrategy decorationStrategy) {
    super(databaseReaders, decorationStrategy);
  }

  @Override
  IpInformation decorateIpInformation(
      IpInformation ipInfo, VpnDecoratorInformation decorativeInfo, IP start, IP end) {
    return IpInformation.builder(ipInfo)
        .isVpn(true)
        .withStartOfRange(start)
        .withEndOfRange(end)
        .build();
  }
}
