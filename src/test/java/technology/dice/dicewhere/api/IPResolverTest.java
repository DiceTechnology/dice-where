package technology.dice.dicewhere.api;


import com.google.common.net.InetAddresses;
import org.junit.Assert;
import org.junit.Test;
import technology.dice.dicewhere.api.api.IP;
import technology.dice.dicewhere.api.api.IPInformation;
import technology.dice.dicewhere.api.api.IPResolver;
import technology.dice.dicewhere.api.exceptions.DuplicateProviderException;
import technology.dice.dicewhere.api.exceptions.NoProvidersException;
import technology.dice.dicewhere.api.exceptions.ProviderNotAvailableException;
import technology.dice.dicewhere.building.DatabaseBuilderListener;
import technology.dice.dicewhere.lineprocessing.SerializedLine;
import technology.dice.dicewhere.parsing.provider.DatabaseProvider;
import technology.dice.dicewhere.reading.provider.dbip.DbIpLineReader;
import technology.dice.dicewhere.reading.provider.maxmind.MaxmindDbReader;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Optional;


public class IPResolverTest {
	@Test(expected = DuplicateProviderException.class)
	public void noProviders() throws IOException {
		new IPResolver.Builder()
				.withProvider(
						new DbIpLineReader(
								Paths.get(IPResolverTest.class.getClassLoader().getResource("provider/dbip/tinyValid.csv").getFile())
						)
				)
				.withProvider(
						new DbIpLineReader(
								Paths.get(IPResolverTest.class.getClassLoader().getResource("provider/dbip/tinyValid.csv").getFile())
						)
				)
				.build();
	}

	@Test(expected = ProviderNotAvailableException.class)
	public void resolveWithUnavailableProvider() throws IOException {
		new IPResolver.Builder()
				.withProvider(
						new DbIpLineReader(
								Paths.get(IPResolverTest.class.getClassLoader().getResource("provider/dbip/tinyValid.csv").getFile())
						)
				)
				.build()
				.resolve("1.1.1.1", DatabaseProvider.MAXMIND);
	}

	@Test(expected = NoProvidersException.class)
	public void duplicatedProviders() throws IOException {
		new IPResolver.Builder().build();
	}

	private IPResolver baseResolver() throws IOException {
		return new IPResolver.Builder()
				.withProvider(
						new MaxmindDbReader(
								Paths.get(IPResolverTest.class.getClassLoader().getResource("provider/maxmind/GeoLite2-City-Locations-en.csv.zip").getFile()),
								Paths.get(IPResolverTest.class.getClassLoader().getResource("provider/maxmind/tinyValidV4.csv").getFile()),
								Paths.get(IPResolverTest.class.getClassLoader().getResource("provider/maxmind/tinyValidV6.csv").getFile())
						))
				.withProvider(
						new DbIpLineReader(
								Paths.get(IPResolverTest.class.getClassLoader().getResource("provider/dbip/tinyValid.csv").getFile())
						)
				)
				.build();
	}

	@Test
	public void existingDualLookupV4() throws IOException {
		IPResolver resolver = this.baseResolver();
		Map<DatabaseProvider, Optional<IPInformation>> result = resolver.resolve("1.0.8.17");
		IPInformation expectedBoth = new IPInformation(
				"CN",
				"1809858",
				"Guangzhou",
				"Guangdong",
				null,
				null,
				new IP(InetAddresses.forString("1.0.8.0")),
				new IP(InetAddresses.forString("1.0.15.255")),
				null

		);
		Assert.assertNotNull(result.get(DatabaseProvider.MAXMIND));
		Assert.assertTrue(result.get(DatabaseProvider.MAXMIND).isPresent());
		Assert.assertEquals(expectedBoth, result.get(DatabaseProvider.MAXMIND).get());
		Assert.assertNotNull(result.get(DatabaseProvider.DBIP));
		Assert.assertTrue(result.get(DatabaseProvider.DBIP).isPresent());
		Assert.assertEquals(expectedBoth, result.get(DatabaseProvider.DBIP).get());
	}

	@Test
	public void existingDualLookupV6() throws IOException {
		IPResolver resolver = this.baseResolver();
		Map<DatabaseProvider, Optional<IPInformation>> result = resolver.resolve("2001:200:2:ffff:ffff:ffff:ffff:0000");
		IPInformation expectedBoth = new IPInformation(
				"JP",
				"1861060",
				null,
				null,
				null,
				null,
				new IP(InetAddresses.forString("2001:200:2::")),
				new IP(InetAddresses.forString("2001:200:3:ffff:ffff:ffff:ffff:ffff")),
				null

		);
		Assert.assertNotNull(result.get(DatabaseProvider.MAXMIND));
		Assert.assertTrue(result.get(DatabaseProvider.MAXMIND).isPresent());
		Assert.assertEquals(expectedBoth, result.get(DatabaseProvider.MAXMIND).get());
		Assert.assertNotNull(result.get(DatabaseProvider.DBIP));
		Assert.assertTrue(result.get(DatabaseProvider.DBIP).isPresent());
		Assert.assertEquals(expectedBoth, result.get(DatabaseProvider.DBIP).get());
	}

	@Test
	public void justAboveIPV4EndDbIp() throws IOException {
		IPResolver resolver = this.baseResolver();
		Optional<IPInformation> dbIp = resolver.resolve("217.207.198.104", DatabaseProvider.DBIP);
		Assert.assertFalse(dbIp.isPresent());
	}

	@Test
	public void justAboveIPV4EndMaxmind() throws IOException {
		IPResolver resolver = this.baseResolver();
		Optional<IPInformation> maxmind = resolver.resolve("1.0.64.0", DatabaseProvider.MAXMIND);
		Assert.assertFalse(maxmind.isPresent());
	}

	@Test
	public void justAboveIPV6EndDbIp() throws IOException {
		IPResolver resolver = this.baseResolver();
		Optional<IPInformation> dbIp = resolver.resolve("2c0f:fff1:0000:0000:0000:0000:0000:0000", DatabaseProvider.DBIP);
		Assert.assertFalse(dbIp.isPresent());
	}

	@Test
	public void justAboveIPV6EndMaxmind() throws IOException {
		IPResolver resolver = this.baseResolver();
		Optional<IPInformation> maxmind = resolver.resolve("2001:200:20:0000:0000:0000:0000:0000", DatabaseProvider.MAXMIND);
		Assert.assertFalse(maxmind.isPresent());
	}

	@Test
	public void justBelowPV4EndDbIp() throws IOException {
		IPResolver resolver = this.baseResolver();
		Optional<IPInformation> dbIp = resolver.resolve("1.0.7.255", DatabaseProvider.DBIP);
		Assert.assertFalse(dbIp.isPresent());
	}

	@Test
	public void justBelowIPV4EndMaxmind() throws IOException {
		IPResolver resolver = this.baseResolver();
		Optional<IPInformation> maxmind = resolver.resolve("0.255.255.255", DatabaseProvider.MAXMIND);
		Assert.assertFalse(maxmind.isPresent());
	}

	@Test
	public void justBelowIPV6EndDbIp() throws IOException {
		IPResolver resolver = this.baseResolver();
		Optional<IPInformation> dbIp = resolver.resolve("2001:200:1:ffff:ffff:ffff:ffff:ffff", DatabaseProvider.DBIP);
		Assert.assertFalse(dbIp.isPresent());
	}

	@Test
	public void justBelowIPV6EndMaxmind() throws IOException {
		IPResolver resolver = this.baseResolver();
		Optional<IPInformation> maxmind = resolver.resolve("600:8801:9400:5a1:948b:ab15:dde3:61a2", DatabaseProvider.MAXMIND);
		Assert.assertFalse(maxmind.isPresent());
	}

	@Test
	public void gapIPV4DbIp() throws IOException {
		IPResolver resolver = this.baseResolver();
		Optional<IPInformation> dbIp = resolver.resolve("88.55.48.100", DatabaseProvider.DBIP);
		Assert.assertFalse(dbIp.isPresent());
	}

	@Test
	public void gapIPV4Maxmind() throws IOException {
		IPResolver resolver = this.baseResolver();
		Optional<IPInformation> dbIp = resolver.resolve("1.0.3.100", DatabaseProvider.MAXMIND);
		Assert.assertFalse(dbIp.isPresent());
	}

	@Test
	public void gapIPV6DbIp() throws IOException {
		IPResolver resolver = this.baseResolver();
		Optional<IPInformation> dbIp = resolver.resolve("2601:6c24:25ff::", DatabaseProvider.DBIP);
		Assert.assertFalse(dbIp.isPresent());
	}

	@Test
	public void gapIPV6Maxmind() throws IOException {
		IPResolver resolver = this.baseResolver();
		Optional<IPInformation> dbIp = resolver.resolve("2001:200:6::", DatabaseProvider.MAXMIND);
		Assert.assertFalse(dbIp.isPresent());
	}

	@Test
	public void ipV6ReadFromIpV4Maxmind() throws IOException {
		IPResolver resolver = this.baseResolver();
		Map<DatabaseProvider, Optional<IPInformation>> result = resolver.resolve("0:0:0:0:0:ffff:100:a");
		IPInformation expectedBoth = new IPInformation(
				"AU",
				"2065740",
				"Morphett Vale",
				"South Australia",
				null,
				"5162",
				new IP(InetAddresses.forString("1.0.0.0")),
				new IP(InetAddresses.forString("1.0.0.63")),
				null

		);
		Assert.assertNotNull(result.get(DatabaseProvider.MAXMIND));
		Assert.assertTrue(result.get(DatabaseProvider.MAXMIND).isPresent());
		Assert.assertEquals(expectedBoth, result.get(DatabaseProvider.MAXMIND).get());
		Assert.assertNotNull(result.get(DatabaseProvider.DBIP));
		Assert.assertFalse(result.get(DatabaseProvider.DBIP).isPresent());
	}

	@Test
	public void ipV6ReadFromIpV4DbIp() throws IOException {
		IPResolver resolver = this.baseResolver();
		Map<DatabaseProvider, Optional<IPInformation>> result = resolver.resolve("0:0:0:0:0:ffff:75e3:1b3a");
		IPInformation expectedBoth = new IPInformation(
				"IN",
				"1269092",
				"Jeypore",
				"Odisha",
				"Koraput",
				"764002",
				new IP(InetAddresses.forString("117.227.27.58")),
				new IP(InetAddresses.forString("117.227.27.58")),
				null

		);
		Assert.assertNotNull(result.get(DatabaseProvider.DBIP));
		Assert.assertTrue(result.get(DatabaseProvider.DBIP).isPresent());
		Assert.assertEquals(expectedBoth, result.get(DatabaseProvider.DBIP).get());
		Assert.assertNotNull(result.get(DatabaseProvider.MAXMIND));
		Assert.assertFalse(result.get(DatabaseProvider.MAXMIND).isPresent());
	}

	@Test
	public void outOfOrderDatabaseDbIp() throws IOException {
		new IPResolver.Builder()
				.withProvider(
						new DbIpLineReader(
								Paths.get(IPResolverTest.class.getClassLoader().getResource("provider/dbip/tinyNotSorted.csv").getFile())
						)
				)
				.withBuilderListener(new DatabaseBuilderListener() {
					@Override
					public void lineOutOfOrder(DatabaseProvider provider, SerializedLine serializedLine, Exception e) {
						throw new RuntimeException(e);
					}
				})
				.build();
	}

	@Test
	public void outOfOrderDatabaseDbMaxmindWithIPV6EquivalentToIPV4() throws IOException {
		new IPResolver.Builder()
				.withProvider(
						new MaxmindDbReader(
								Paths.get(IPResolverTest.class.getClassLoader().getResource("provider/maxmind/GeoLite2-City-Locations-en.csv.zip").getFile()),
								Paths.get(IPResolverTest.class.getClassLoader().getResource("provider/maxmind/tinyValidV4.csv").getFile()),
								Paths.get(IPResolverTest.class.getClassLoader().getResource("provider/maxmind/containsLowerIpv4V6.csv").getFile())
						))
				.withBuilderListener(new DatabaseBuilderListener() {
					@Override
					public void lineOutOfOrder(DatabaseProvider provider, SerializedLine serializedLine, Exception e) {
						throw new RuntimeException(e);
					}
				})
				.build();
	}
}
