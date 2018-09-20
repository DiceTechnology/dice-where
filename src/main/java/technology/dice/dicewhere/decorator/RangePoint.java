package technology.dice.dicewhere.decorator;

import technology.dice.dicewhere.api.api.IP;

public class RangePoint<T> {
  private final IP ip;
  private final boolean isStart;
  private final T rangeInfo;

  RangePoint(IP ip, boolean isStart, T rangeInfo) {
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
}
