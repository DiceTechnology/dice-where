/*
 * Copyright (C) 2018 - present by Dice Technology Ltd.
 *
 * Please see distribution for license.
 */

package technology.dice.dicewhere.decorator;

import technology.dice.dicewhere.api.api.IP;
import technology.dice.dicewhere.api.api.IpInformation;

public class DecorationRangePoint<T> extends RangePoint<T> {

  private final IpInformation ipInformation;

  public DecorationRangePoint(IP ip, boolean isStart, T info, IpInformation ipInformation) {
    super(ip, isStart, info);
    this.ipInformation = ipInformation;
  }

  public DecorationRangePoint(IP ip, boolean isStart, IpInformation ipInformation) {
    this(ip, isStart, null, ipInformation);
  }

  public IpInformation getIpInformation() {
    return ipInformation;
  }

}
