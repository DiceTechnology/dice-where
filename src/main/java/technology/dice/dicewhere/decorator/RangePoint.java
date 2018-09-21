/*
 * Copyright (C) 2018 - present by Dice Technology Ltd.
 *
 * Please see distribution for license.
 */

package technology.dice.dicewhere.decorator;

import technology.dice.dicewhere.api.api.IP;

import java.util.Objects;

public class RangePoint<T> {
  private final IP ip;
  private final boolean isStart;
  private final T rangeInfo;

  public RangePoint(IP ip, boolean isStart, T rangeInfo) {
    this.ip = ip;
    this.isStart = isStart;
    this.rangeInfo = rangeInfo;
  }

  IP getIp() {
    return ip;
  }

  boolean isStart() {
    return isStart;
  }

  T getRangeInfo() {
    return rangeInfo;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    RangePoint<?> that = (RangePoint<?>) o;
    return isStart() == that.isStart()
        && Objects.equals(getIp(), that.getIp())
        && Objects.equals(getRangeInfo(), that.getRangeInfo());
  }

  @Override
  public int hashCode() {
    return Objects.hash(getIp(), isStart(), getRangeInfo());
  }

  @Override
  public String toString() {
    return "RangePoint{"
        + "ip="
        + ip
        + ", isStart="
        + isStart
        + ", rangeInfo="
        + rangeInfo
        + '}';
  }
}
