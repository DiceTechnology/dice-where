/*
 * Copyright (C) 2018 - present by Dice Technology Ltd.
 *
 * Please see distribution for license.
 */

package technology.dice.dicewhere.decorator;

import technology.dice.dicewhere.api.api.IP;
import technology.dice.dicewhere.api.api.IpInformation;

import java.util.Collection;
import java.util.Optional;

/**
 * Implementation of Decorator that marks all IpInformation ranges as VPN if they are within the
 * provided DBs
 */
public class VpnDecorator extends Decorator<VpnDecoratorInformation> {

  public VpnDecorator(
      Collection<DecoratorDbReader<VpnDecoratorInformation>> databaseReaders,
      DecorationStrategy decorationStrategy) {
    super(databaseReaders, decorationStrategy);
  }

  @Override
  IpInformation decorateIpInformationMatch(
      IpInformation ipInfo,
      DecorationRangePoint<Optional<VpnDecoratorInformation>> decorativeInfo,
      IP start,
      IP end) {
    return IpInformation.builder(ipInfo)
        .isVpn(true)
        .withStartOfRange(start)
        .withEndOfRange(end)
        .build();
  }

  @Override
  IpInformation decorateIpInformationMiss(
      IpInformation ipInfo,
      DecorationRangePoint<Optional<VpnDecoratorInformation>> decorativeInfo,
      IP start,
      IP end) {
    return IpInformation.builder(ipInfo)
        .isVpn(false)
        .withStartOfRange(start)
        .withEndOfRange(end)
        .build();
  }
}
