package technology.dice.dicewhere.reading.provider.maxmind;

import java.util.Objects;

public class MaxmindLocation {
	public final static MaxmindLocation UNKNOWN = new MaxmindLocation("", "ZZ", "", "", "");

	private final String geonameId;
	private final String countryCodeAlpha2;
	private final String mostSpecificDivision;
	private final String leastSpecificDivision;
	private final String city;

	public MaxmindLocation(String geonameId, String countryCodeAlpha2, String mostSpecificDivision, String leastSpecificDivision, String city) {
		this.geonameId = geonameId;
		this.countryCodeAlpha2 = countryCodeAlpha2;
		this.mostSpecificDivision = mostSpecificDivision;
		this.leastSpecificDivision = leastSpecificDivision;
		this.city = city;
	}

	public String getGeonameId() {
		return geonameId;
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
		return Objects.equals(geonameId, that.geonameId) &&
				Objects.equals(countryCodeAlpha2, that.countryCodeAlpha2) &&
				Objects.equals(mostSpecificDivision, that.mostSpecificDivision) &&
				Objects.equals(leastSpecificDivision, that.leastSpecificDivision) &&
				Objects.equals(city, that.city);
	}

	@Override
	public int hashCode() {

		return Objects.hash(geonameId, countryCodeAlpha2, mostSpecificDivision, leastSpecificDivision, city);
	}

}
