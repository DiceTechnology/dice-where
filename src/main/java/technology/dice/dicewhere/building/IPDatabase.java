/*
 * Copyright (C) 2018 - present by Dice Technology Ltd.
 *
 * Please see distribution for license.
 */

package technology.dice.dicewhere.building;

import com.google.protobuf.InvalidProtocolBufferException;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Optional;

import technology.dice.dicewhere.api.api.AnonymousState;
import technology.dice.dicewhere.api.api.IP;
import technology.dice.dicewhere.api.api.IpInformation;
import technology.dice.dicewhere.lineprocessing.serializers.protobuf.IPInformationProto;

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
        .flatMap(
            entry -> {
              try {
                IPInformationProto.IpInformationProto ipInformationProto =
                    IPInformationProto.IpInformationProto.parseFrom(entry.getValue());
                IpInformation ipInformation =
                    IpInformation.builder()
                        .withCountryCodeAlpha2(ipInformationProto.getCountryCodeAlpha2())
                        .withGeonameId(ipInformationProto.getGeonameId())
                        .withCity(ipInformationProto.getCity())
                        .withLeastSpecificDivision(ipInformationProto.getLeastSpecificDivision())
                        .withMostSpecificDivision(ipInformationProto.getMostSpecificDivision())
                        .withPostcode(ipInformationProto.getPostcode())
                        .withStartOfRange(
                            new IP(ipInformationProto.getStartOfRange().toByteArray()))
                        .withEndOfRange(new IP(ipInformationProto.getEndOfRange().toByteArray()))
                        .isVpn(ipInformationProto.getIsVpn())
                        .withOriginalLine(
                            "".equals(ipInformationProto.getOriginalLine())
                                    || ipInformationProto.getOriginalLine() == null
                                ? null
                                : ipInformationProto.getOriginalLine())
                        .build();

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
