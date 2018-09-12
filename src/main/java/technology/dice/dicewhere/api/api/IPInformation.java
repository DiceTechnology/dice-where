package technology.dice.dicewhere.api.api;

import javax.annotation.Nullable;
import javax.annotation.Nonnull;
import java.io.Serializable;
import java.util.Objects;
import java.util.Optional;

public class IPInformation implements Serializable {
	private final Optional<String> originalLine;
	private final String countryCodeAlpha2;
	private final Optional<String> geonameId;
	private final Optional<String> city;
	private final Optional<String> leastSpecificDivision;
	private final Optional<String> mostSpecificDivision;
	private final Optional<String> postcode;
	private final IP startOfRange;
	private final IP endOfRange;

	/**
	 * @param countryCodeAlpha2     the country of this location
	 * @param geonameId             the geoname identifier of this location (https://www.geonames.org/) This field will be Optional.empty() if an empty string is passed
	 * @param city					the city of this location. This field will be Optional.empty() if an empty string is passed
	 * @param leastSpecificDivision the least specific administrative division of this location. This field will be Optional.empty() if an empty string is passed
	 * @param mostSpecificDivision  the most specific administrative division of this location. This field will be Optional.empty() if an empty string is passed
	 * @param postcode              the postcode of this location. This field will be Optional.empty() if an empty string is passed
	 * @param startOfRange			the first IP of the range of IPs located in this location
	 * @param endOfRange            the last IP of the range of IPs located in this location
	 * @param originalLine          the database line that got processed into this location object
	 */
	public IPInformation(@Nonnull String countryCodeAlpha2,
						 @Nullable String geonameId,
						 @Nullable String city,
						 @Nullable String leastSpecificDivision,
						 @Nullable String mostSpecificDivision,
						 @Nullable String postcode,
						 @Nonnull IP startOfRange,
						 @Nonnull IP endOfRange,
						 @Nullable String originalLine
	) {
		this.countryCodeAlpha2 = Objects.requireNonNull(countryCodeAlpha2);
		this.geonameId = Optional.ofNullable("".equals(geonameId) ? null : geonameId);
		this.city = Optional.ofNullable("".equals(city) ? null : city);
		this.leastSpecificDivision = Optional.ofNullable("".equals(leastSpecificDivision) ? null : leastSpecificDivision);
		this.mostSpecificDivision = Optional.ofNullable("".equals(mostSpecificDivision) ? null : mostSpecificDivision);
		this.postcode = Optional.ofNullable("".equals(postcode) ? null : postcode);
		this.startOfRange = Objects.requireNonNull(startOfRange);
		this.endOfRange = Objects.requireNonNull(endOfRange);
		this.originalLine = Optional.ofNullable(originalLine);
	}

	public String getCountryCodeAlpha2() {
		return countryCodeAlpha2;
	}

	public Optional<String> getGeonameId() {
		return geonameId;
	}

	public Optional<String> getCity() {
		return city;
	}

	public Optional<String> getLeastSpecificDivision() {
		return leastSpecificDivision;
	}

	public Optional<String> getMostSpecificDivision() {
		return mostSpecificDivision;
	}

	public Optional<String> getPostcode() {
		return postcode;
	}

	public IP getStartOfRange() {
		return startOfRange;
	}

	public IP getEndOfRange() {
		return endOfRange;
	}

	public Optional<String> getOriginalLine() {
		return originalLine;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof IPInformation)) {
			return false;
		}
		IPInformation that = (IPInformation) o;
		return Objects.equals(originalLine, that.originalLine) &&
				Objects.equals(countryCodeAlpha2, that.countryCodeAlpha2) &&
				Objects.equals(geonameId, that.geonameId) &&
				Objects.equals(city, that.city) &&
				Objects.equals(leastSpecificDivision, that.leastSpecificDivision) &&
				Objects.equals(mostSpecificDivision, that.mostSpecificDivision) &&
				Objects.equals(postcode, that.postcode) &&
				Objects.equals(startOfRange, that.startOfRange) &&
				Objects.equals(endOfRange, that.endOfRange);
	}

	@Override
	public int hashCode() {
		return Objects.hash(originalLine, countryCodeAlpha2, geonameId, city, leastSpecificDivision, mostSpecificDivision, postcode, startOfRange, endOfRange);
	}

}
