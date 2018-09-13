package technology.dice.dicewhere.building;

import com.google.protobuf.InvalidProtocolBufferException;
import technology.dice.dicewhere.api.api.IP;
import technology.dice.dicewhere.api.api.IpInformation;
import technology.dice.dicewhere.lineprocessing.serializers.protobuf.IPInformationProto;

import java.util.Map;
import java.util.NavigableMap;
import java.util.Optional;

public class IPDatabase {
	private final NavigableMap<IP, byte[]> db;
	private final long size;

	IPDatabase(NavigableMap<IP, byte[]> db) {
		this.db = db;
		this.size = db.size();
	}

	public Optional<IpInformation> get(IP ip) {
		Map.Entry<IP, byte[]> ipEntry = db.floorEntry(ip);
		return Optional.ofNullable(ipEntry)
				.flatMap(entry -> {
					try {
						IPInformationProto.IpInformationProto ipInformationProto = IPInformationProto.IpInformationProto.parseFrom(entry.getValue());
						IpInformation ipInformation = new IpInformation(
								ipInformationProto.getCountryCodeAlpha2(),
								ipInformationProto.getGeonameId(),
								ipInformationProto.getCity(),
								ipInformationProto.getLeastSpecificDivision(),
								ipInformationProto.getMostSpecificDivision(),
								ipInformationProto.getPostcode(),
								new IP(ipInformationProto.getStartOfRange().toByteArray()),
								new IP(ipInformationProto.getEndOfRange().toByteArray()),
								"".equals(ipInformationProto.getOriginalLine()) || ipInformationProto.getOriginalLine() == null ? null : ipInformationProto.getOriginalLine());

						if (ip.isGreaterThan(ipInformation.getEndOfRange())) {
							return Optional.empty();
						}

						return Optional.of(ipInformation);

					} catch (InvalidProtocolBufferException e) {
						throw new RuntimeException(e);
					}
				});
	}

	public long size() {
		return size;
	}
}
