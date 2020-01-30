/*
 * Copyright (C) 2018 - present by Dice Technology Ltd.
 *
 * Please see distribution for license.
 */

package technology.dice.dicewhere.decorator;

import technology.dice.dicewhere.api.api.IP;

import java.util.Objects;

public class VpnDecoratorInformation implements DecoratorInformation {
  private final IP rangeStart;
  private final IP rangeEnd;
  private final int numberOfMatches;

  public VpnDecoratorInformation(IP rangeStart, IP rangeEnd, int numberOfMatches) {
    this.rangeStart = rangeStart;
    this.rangeEnd = rangeEnd;
    this.numberOfMatches = numberOfMatches;
  }

  public VpnDecoratorInformation(IP rangeStart, IP rangeEnd) {
    this(rangeStart, rangeEnd, 1);
  }

  @Override
  public IP getRangeStart() {
    return rangeStart;
  }

  @Override
  public IP getRangeEnd() {
    return rangeEnd;
  }

  @Override
  public int getNumberOfMatches() {
    return numberOfMatches;
  }

  @Override
  public VpnDecoratorInformation withNewRange(IP start, IP end) {
    return new VpnDecoratorInformation(start, end, numberOfMatches);
  }

  @Override
  public VpnDecoratorInformation withNumberOfMatches(int i) {
    return new VpnDecoratorInformation(rangeStart, rangeEnd, i);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    VpnDecoratorInformation that = (VpnDecoratorInformation) o;
    return numberOfMatches == that.numberOfMatches
        && Objects.equals(rangeStart, that.rangeStart)
        && Objects.equals(rangeEnd, that.rangeEnd);
  }

  @Override
  public int hashCode() {
    return Objects.hash(rangeStart, rangeEnd, numberOfMatches);
  }

  @Override
  public String toString() {
    return "VpnDecoratorInformation{"
        + "rangeStart="
        + rangeStart
        + ", rangeEnd="
        + rangeEnd
        + ", numberOfMatches="
        + numberOfMatches
        + '}';
  }

}
