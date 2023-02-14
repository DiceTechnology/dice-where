/*
 * Copyright (C) 2018 - present by Dice Technology Ltd.
 *
 * Please see distribution for license.
 */

package technology.dice.dicewhere.provider.maxmind.reading;

import java.util.Objects;

public class MaxmindLocation {
  public static final MaxmindLocation UNKNOWN = new MaxmindLocation("", "ZZ", "", "", "");

  private final String cityGeonameId;
  private final String countryCodeAlpha2;
  private final String mostSpecificDivision;
  private final String leastSpecificDivision;
  private final String city;

  public MaxmindLocation(
      String cityGeonameId,
      String countryCodeAlpha2,
      String mostSpecificDivision,
      String leastSpecificDivision,
      String city) {
    this.cityGeonameId = cityGeonameId;
    this.countryCodeAlpha2 = countryCodeAlpha2;
    this.mostSpecificDivision = mostSpecificDivision;
    this.leastSpecificDivision = leastSpecificDivision;
    this.city = city;
  }

  public String getCityGeonameId() {
    return cityGeonameId;
  }

  public String getCountryCodeAlpha2() {
    return countryCodeAlpha2;
  }

  public String getMostSpecificDivision() {
    return mostSpecificDivision;
  }

  public String getLeastSpecificDivision() {
    return leastSpecificDivision;
  }

  public String getCity() {
    return city;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof MaxmindLocation)) {
      return false;
    }
    MaxmindLocation that = (MaxmindLocation) o;
    return Objects.equals(cityGeonameId, that.cityGeonameId)
        && Objects.equals(countryCodeAlpha2, that.countryCodeAlpha2)
        && Objects.equals(mostSpecificDivision, that.mostSpecificDivision)
        && Objects.equals(leastSpecificDivision, that.leastSpecificDivision)
        && Objects.equals(city, that.city);
  }

  @Override
  public int hashCode() {

    return Objects.hash(
        cityGeonameId, countryCodeAlpha2, mostSpecificDivision, leastSpecificDivision, city);
  }
}
