package technology.dice.dicewhere;

import com.google.common.net.InetAddresses;
import technology.dice.dicewhere.api.api.IP;
import technology.dice.dicewhere.api.api.IpInformation;
import technology.dice.dicewhere.api.api.IPResolver;
import technology.dice.dicewhere.parsing.provider.DatabaseProvider;
import technology.dice.dicewhere.reading.LineReaderListener;
import technology.dice.dicewhere.reading.RawLine;
import technology.dice.dicewhere.reading.provider.dbip.DbIpLineReader;
import technology.dice.dicewhere.reading.provider.maxmind.MaxmindDbReader;

import java.io.IOException;
import java.net.InetAddress;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Optional;
import java.util.Scanner;
import java.util.concurrent.ExecutionException;

public class Main {
	public static void maisn(String[] args) throws IOException {
		String IPV4 = "192.168.4.5";
		String IPV6 = "0:0:0:0:0:ffff:c0a8:405";
		InetAddress inetAddressV4 = InetAddresses.forString(IPV4);
		InetAddress inetAddressV6 = InetAddresses.forString(IPV6);
		System.out.println(inetAddressV4.equals(inetAddressV6));
		byte[] ipv4b = inetAddressV4.getAddress();
		byte[] ipv6b = inetAddressV6.getAddress();
		System.out.println(ipv4b.equals(ipv6b));

	}

	public static void main(String[] args) throws IOException, ExecutionException, InterruptedException {
		InetAddress a;
		System.in.read();
		System.out.println("Started");
		long before = System.currentTimeMillis();

		long beforeMM = System.currentTimeMillis();
		long afterMM = System.currentTimeMillis();
		System.out.println("Binary MM took " + (afterMM - beforeMM));


		IPResolver.Builder resolverBuilder = new IPResolver.Builder()
				.withProvider(
						new MaxmindDbReader(
								Paths.get("/Users/gluiz/Downloads/GeoIP2-City-CSV_20180703/GeoIP2-City-Locations-en.csv.zip"),
								Paths.get("/Users/gluiz/Downloads/GeoIP2-City-CSV_20180703/GeoIP2-City-Blocks-IPv4.csv.zip"),
								Paths.get("/Users/gluiz/Downloads/GeoIP2-City-CSV_2018070D3/GeoIP2-City-Blocks-IPv6.csv")
						))
				.withProvider(
						new DbIpLineReader(Paths.get("/Users/gluiz/dev/geoip2-poc/src/test/resources/provider/dbip/dbip-country-2018-07.csv.gz"))
						//new DbIpLineReader(Paths.get("/Users/gluiz/Downloads/dbip-full-2018-07.csv"))
				)
				.withReaderListener(new LineReaderListener() {
					@Override
					public void lineRead(DatabaseProvider provider, RawLine rawLine, long elapsedMillis) {
						if (rawLine.getLineNumber() % 100000 == 0) {
							System.out.println(Thread.currentThread().getName() + " ##### Read " + rawLine.getLineNumber() + " records so far in + " + elapsedMillis / 1e3 + " seconds.");
						}
					}

					@Override
					public void finished(DatabaseProvider provider, long linesProcessed, long elapsedMillis) {
						System.out.println("Finished processing " + linesProcessed + " lines in " + elapsedMillis / 1e3 + " seconds");
					}
				});

		IPResolver resolver = resolverBuilder.build();


		Scanner keyboard = new Scanner(System.in);
		while (true) {
			System.out.print("Enter an IP:>");
			String ipToLookupString = keyboard.nextLine();
			if ("exit".equalsIgnoreCase(ipToLookupString)) {
				break;
			} else {
				System.out.println();
				try {
					InetAddress ipToLookup = InetAddresses.forString(ipToLookupString);
					long beforeLookup = System.nanoTime();
					print(new IP(ipToLookup), resolver);
					long afterLookup = System.nanoTime();
					System.out.println("both queries took " + (afterLookup - beforeLookup) / 1e6 + " ms");
				} catch (IllegalArgumentException e) {
					System.out.println("Invalid IP, try again.");
				}
			}
		}
		System.out.println("EXIT");
	}


	private static void print(IP ip, IPResolver resolver) {
		long beforeLookup = System.nanoTime();
		Map<DatabaseProvider, Optional<IpInformation>> resolve = resolver.resolve(ip);
		long afterLookup = System.nanoTime();
		for (Optional<IpInformation> found : resolve.values()) {
			{
				if (found.isPresent()) {
					found.ifPresent(
							info ->
							{
								System.out.println("That IP is in [" + info.getCountryCodeAlpha2() + "," +
										"" + info.getLeastSpecificDivision() + "," +
										"" + info.getMostSpecificDivision() + "," +
										"" + info.getCity() + "," +
										"" + info.getPostcode() +
										"]. GeonameId=" + info.getGeonameId() + "(took " + (afterLookup - beforeLookup) / 1e6 + " ms)");
								info.getOriginalLine().ifPresent(ol -> System.err.println("Original line: " + ol));

							});
				} else {
					System.out.println("IP not found");
				}
			}
		}
	}
}
