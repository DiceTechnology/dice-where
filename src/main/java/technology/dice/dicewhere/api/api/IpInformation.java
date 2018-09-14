package technology.dice.dicewhere.api.api;

import java.util.Objects;
import java.util.Optional;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import technology.dice.dicewhere.lineprocessing.serializers.protobuf.ThreeStateValueProto;
import technology.dice.dicewhere.utils.ProtoValueConverter;
import technology.dice.dicewhere.utils.StringUtils;

public class IpInformation {
  private final String originalLine;
  private final String countryCodeAlpha2;
  private final String geonameId;
  private final String city;
  private final String leastSpecificDivision;
  private final String mostSpecificDivision;
  private final String postcode;
  private final Boolean isVpn;
  private final IP startOfRange;
  private final IP endOfRange;

  /**
   * @param countryCodeAlpha2 the country of this location
   * @param geonameId the geoname identifier of this location (https://www.geonames.org/) This field
   *     will be Optional.empty() if an empty string is passed
   * @param city the city of this location. This field will be Optional.empty() if an empty string
   *     is passed
   * @param leastSpecificDivision the least specific administrative division of this location. This
   *     field will be Optional.empty() if an empty string is passed
   * @param mostSpecificDivision the most specific administrative division of this location. This
   *     field will be Optional.empty() if an empty string is passed
   * @param postcode the postcode of this location. This field will be Optional.empty() if an empty
   *     string is passed
   * @param startOfRange the first IP of the range of IPs located in this location
   * @param endOfRange the last IP of the range of IPs located in this location
   * @param originalLine the database line that got processed into this location object
   * @param isVpn whether this range is marked as VPN from the DB provider
   */
  public IpInformation(
      @Nonnull String countryCodeAlpha2,
      @Nullable String geonameId,
      @Nullable String city,
      @Nullable String leastSpecificDivision,
      @Nullable String mostSpecificDivision,
      @Nullable String postcode,
      @Nonnull IP startOfRange,
      @Nonnull IP endOfRange,
      @Nullable String originalLine,
      @Nullable Boolean isVpn) {
    this.countryCodeAlpha2 = Objects.requireNonNull(countryCodeAlpha2);
    this.geonameId = geonameId;
    this.city = city;
    this.leastSpecificDivision = leastSpecificDivision;
    this.mostSpecificDivision = mostSpecificDivision;
    this.postcode = postcode;
    this.startOfRange = Objects.requireNonNull(startOfRange);
    this.endOfRange = Objects.requireNonNull(endOfRange);
    this.originalLine = originalLine;
    this.isVpn = isVpn;
  }

  public String getCountryCodeAlpha2() {
    return countryCodeAlpha2;
  }

  public Optional<String> getGeonameId() {
    return StringUtils.nonEmptyString(geonameId);
  }

  public Optional<String> getCity() {
    return StringUtils.nonEmptyString(city);
  }

  public Optional<String> getLeastSpecificDivision() {
    return StringUtils.nonEmptyString(leastSpecificDivision);
  }

  public Optional<String> getMostSpecificDivision() {
    return StringUtils.nonEmptyString(mostSpecificDivision);
  }

  public Optional<String> getPostcode() {
    return StringUtils.nonEmptyString(postcode);
  }

  public IP getStartOfRange() {
    return startOfRange;
  }

  public IP getEndOfRange() {
    return endOfRange;
  }

  public Optional<String> getOriginalLine() {
    return StringUtils.nonEmptyString(originalLine);
  }

  public Optional<Boolean> isVpn() {
    return Optional.ofNullable(isVpn);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof IpInformation)) {
      return false;
    }
    IpInformation that = (IpInformation) o;
    return Objects.equals(getOriginalLine(), that.getOriginalLine())
        && Objects.equals(getCountryCodeAlpha2(), that.getCountryCodeAlpha2())
        && Objects.equals(getGeonameId(), that.getGeonameId())
        && Objects.equals(getCity(), that.getCity())
        && Objects.equals(getLeastSpecificDivision(), that.getLeastSpecificDivision())
        && Objects.equals(getMostSpecificDivision(), that.getMostSpecificDivision())
        && Objects.equals(getPostcode(), that.getPostcode())
        && Objects.equals(getStartOfRange(), that.getStartOfRange())
        && Objects.equals(isVpn(), that.isVpn())
        && Objects.equals(getEndOfRange(), that.getEndOfRange());
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        originalLine,
        countryCodeAlpha2,
        geonameId,
        city,
        leastSpecificDivision,
        mostSpecificDivision,
        postcode,
        startOfRange,
        endOfRange,
        isVpn);
  }

  @Override
  public String toString() {
    return "IpInformation{"
        + "originalLine='"
        + originalLine
        + '\''
        + ", countryCodeAlpha2='"
        + countryCodeAlpha2
        + '\''
        + ", geonameId='"
        + geonameId
        + '\''
        + ", city='"
        + city
        + '\''
        + ", leastSpecificDivision='"
        + leastSpecificDivision
        + '\''
        + ", mostSpecificDivision='"
        + mostSpecificDivision
        + '\''
        + ", postcode='"
        + postcode
        + '\''
        + ", isVpn="
        + isVpn
        + ", startOfRange="
        + startOfRange
        + ", endOfRange="
        + endOfRange
        + '}';
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {
    private String countryCodeAlpha2;
    private String geonameId;
    private String city;
    private String leastSpecificDivision;
    private String mostSpecificDivision;
    private String postcode;
    private IP startOfRange;
    private IP endOfRange;
    private String originalLine;
    private Boolean isVpn;

    private Builder() {}

    public Builder withCountryCodeAlpha2(String countryCodeAlpha2) {
      this.countryCodeAlpha2 = Objects.requireNonNull(countryCodeAlpha2);
      return this;
    }

    public Builder withGeonameId(String geonameId) {
      this.geonameId = geonameId;
      return this;
    }

    public Builder withCity(String city) {
      this.city = city;
      return this;
    }

    public Builder withLeastSpecificDivision(String leastSpecificDivision) {
      this.leastSpecificDivision = leastSpecificDivision;
      return this;
    }

    public Builder withMostSpecificDivision(String mostSpecificDivision) {
      this.mostSpecificDivision = mostSpecificDivision;
      return this;
    }

    public Builder withPostcode(String postcode) {
      this.postcode = postcode;
      return this;
    }

    public Builder withStartOfRange(IP startOfRange) {
      this.startOfRange = Objects.requireNonNull(startOfRange);
      return this;
    }

    public Builder withEndOfRange(IP endOfRange) {
      this.endOfRange = Objects.requireNonNull(endOfRange);
      return this;
    }

    public Builder withOriginalLine(String originalLine) {
      this.originalLine = originalLine;
      return this;
    }

    public Builder isVpn(Optional<Boolean> isVpn) {
      this.isVpn = isVpn.orElse(null);
      return this;
    }

    public Builder isVpn(boolean isVpn) {
      this.isVpn = isVpn;
      return this;
    }
    public Builder isVpn(ThreeStateValueProto.ThreeStateValue isVpn) {
      this.isVpn = ProtoValueConverter.parseThreeStateProto(isVpn).orElse(null);
      return this;
    }

    public IpInformation build() {
      return new IpInformation(
          Objects.requireNonNull(countryCodeAlpha2),
          geonameId,
          city,
          leastSpecificDivision,
          mostSpecificDivision,
          postcode,
          Objects.requireNonNull(startOfRange),
          Objects.requireNonNull(endOfRange),
          originalLine,
          isVpn);
    }
  }
}
