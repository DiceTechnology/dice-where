/*
 * Copyright (C) 2018 - present by Dice Technology Ltd.
 *
 * Please see distribution for license.
 */

package technology.dice.dicewhere.decorator;

import technology.dice.dicewhere.api.api.IP;
import technology.dice.dicewhere.api.api.IpInformation;

class DecorationRangePoint<T> extends RangePoint<T> {

  private final IpInformation ipInformation;

  DecorationRangePoint(IP ip, boolean isStart, T info, IpInformation ipInformation) {
    super(ip, isStart, info);
    this.ipInformation = ipInformation;
  }

  IpInformation getIpInformation() {
    return ipInformation;
  }
}
