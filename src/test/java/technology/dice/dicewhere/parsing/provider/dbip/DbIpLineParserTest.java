package technology.dice.dicewhere.parsing.provider.dbip;


import com.google.common.net.InetAddresses;
import org.junit.Assert;
import org.junit.Test;
import technology.dice.dicewhere.api.api.IP;
import technology.dice.dicewhere.api.api.IPInformation;
import technology.dice.dicewhere.api.exceptions.LineParsingException;
import technology.dice.dicewhere.parsing.ParsedLine;
import technology.dice.dicewhere.reading.RawLine;

public class DbIpLineParserTest {
	@Test
	public void ipV4LineWithOriginalLine() throws LineParsingException {
		DbIpLineParser dbIpLineParser = new DbIpLineParser();
		String line = "1.0.0.0,1.0.0.255,AU,Queensland,Brisbane,\"South Brisbane\",4101,-27.4748,153.017,2207259,10,Australia/Brisbane,\"APNIC Research and Development\",,";
		RawLine rawLine = new RawLine(line, 1);
		ParsedLine parsed = dbIpLineParser.parse(new RawLine(line, 1), true);
		ParsedLine expected = new ParsedLine(
				new IP(InetAddresses.forString("1.0.0.0")),
				new IP(InetAddresses.forString("1.0.0.255")),
				new IPInformation(
						"AU",
						"2207259",
						"South Brisbane",
						"Queensland",
						"Brisbane",
						"4101",
						new IP(InetAddresses.forString("1.0.0.0")),
						new IP(InetAddresses.forString("1.0.0.255")),
						line
				),
				rawLine);
		Assert.assertEquals(expected, parsed);
	}

	@Test
	public void ipV4LineWithoutOriginalLine() throws LineParsingException {
		DbIpLineParser dbIpLineParser = new DbIpLineParser();
		String line = "1.0.0.0,1.0.0.255,AU,Queensland,Brisbane,\"South Brisbane\",4101,-27.4748,153.017,2207259,10,Australia/Brisbane,\"APNIC Research and Development\",,";
		RawLine rawLine = new RawLine(line, 1);
		ParsedLine parsed = dbIpLineParser.parse(new RawLine(line, 1), false);
		ParsedLine expected = new ParsedLine(
				new IP(InetAddresses.forString("1.0.0.0")),
				new IP(InetAddresses.forString("1.0.0.255")),
				new IPInformation(
						"AU",
						"2207259",
						"South Brisbane",
						"Queensland",
						"Brisbane",
						"4101",
						new IP(InetAddresses.forString("1.0.0.0")),
						new IP(InetAddresses.forString("1.0.0.255")),
						null
				),
				rawLine);
		Assert.assertEquals(expected, parsed);
	}

	@Test
	public void ipV6LineWithOriginal() throws LineParsingException {
		DbIpLineParser dbIpLineParser = new DbIpLineParser();
		String line = "2c0f:fa41::,2c0f:fa47:ffff:ffff:ffff:ffff:ffff:ffff,MU,\"Plaines Wilhems\",,\"Ebene CyberCity\",,-20.2419,57.4896,1106748,4,Indian/Mauritius,\"African Network Information Center - ( AfriNIC Ltd )\",,";
		RawLine rawLine = new RawLine(line, 1);
		ParsedLine parsed = dbIpLineParser.parse(new RawLine(line, 1), true);
		ParsedLine expected = new ParsedLine(
				new IP(InetAddresses.forString("2c0f:fa41:0:0:0:0:0:0")),
				new IP(InetAddresses.forString("2c0f:fa47:ffff:ffff:ffff:ffff:ffff:ffff")),
				new IPInformation(
						"MU",
						"1106748",
						"Ebene CyberCity",
						"Plaines Wilhems",
						null,
						null,
						new IP(InetAddresses.forString("2c0f:fa41:0:0:0:0:0:0")),
						new IP(InetAddresses.forString("2c0f:fa47:ffff:ffff:ffff:ffff:ffff:ffff")),
						line
				),
				rawLine);
		Assert.assertEquals(expected, parsed);
	}

	@Test
	public void ipV6LineWithWithoutOriginalLine() throws LineParsingException {
		DbIpLineParser dbIpLineParser = new DbIpLineParser();
		String line = "2c0f:fa41::,2c0f:fa47:ffff:ffff:ffff:ffff:ffff:ffff,MU,\"Plaines Wilhems\",,\"Ebene CyberCity\",,-20.2419,57.4896,1106748,4,Indian/Mauritius,\"African Network Information Center - ( AfriNIC Ltd )\",,";
		RawLine rawLine = new RawLine(line, 1);
		ParsedLine parsed = dbIpLineParser.parse(new RawLine(line, 1), false);
		ParsedLine expected = new ParsedLine(
				new IP(InetAddresses.forString("2c0f:fa41:0:0:0:0:0:0")),
				new IP(InetAddresses.forString("2c0f:fa47:ffff:ffff:ffff:ffff:ffff:ffff")),
				new IPInformation(
						"MU",
						"1106748",
						"Ebene CyberCity",
						"Plaines Wilhems",
						null,
						null,
						new IP(InetAddresses.forString("2c0f:fa41:0:0:0:0:0:0")),
						new IP(InetAddresses.forString("2c0f:fa47:ffff:ffff:ffff:ffff:ffff:ffff")),
						null
				),
				rawLine);
		Assert.assertEquals(expected, parsed);
	}

	@Test(expected = LineParsingException.class)
	public void wrongLineFormat() throws LineParsingException {
		DbIpLineParser dbIpLineParser = new DbIpLineParser();
		String line = "column1,column2,column3";
		try {
			dbIpLineParser.parse(new RawLine(line, 1), false);
		} catch (LineParsingException e) {
			Assert.assertEquals(line, e.getOffendingLine().getLine());
			Assert.assertEquals(1, e.getOffendingLine().getLineNumber());
			throw e;
		}
	}

	@Test(expected = LineParsingException.class)
	public void wrongStartIpFormat() throws LineParsingException {
		DbIpLineParser dbIpLineParser = new DbIpLineParser();

		String line = "uh-Oh,1.0.0.255,AU,Queensland,Brisbane,\"South Brisbane\",4101,-27.4748,153.017,2207259,10,Australia/Brisbane,\"APNIC Research and Development\",,";
		try {
			dbIpLineParser.parse(new RawLine(line, 1), false);
		} catch (LineParsingException e) {
			Assert.assertEquals(line, e.getOffendingLine().getLine());
			Assert.assertEquals(1, e.getOffendingLine().getLineNumber());
			throw e;
		}
	}

	@Test(expected = LineParsingException.class)
	public void wrongEndIpFormat() throws LineParsingException {
		DbIpLineParser dbIpLineParser = new DbIpLineParser();
		String line = "1.0.0.0,stop-Hammertime,AU,Queensland,Brisbane,\"South Brisbane\",4101,-27.4748,153.017,2207259,10,Australia/Brisbane,\"APNIC Research and Development\",,";
		try {
			dbIpLineParser.parse(new RawLine(line, 1), false);
		} catch (LineParsingException e) {
			Assert.assertEquals(line, e.getOffendingLine().getLine());
			Assert.assertEquals(1, e.getOffendingLine().getLineNumber());
			throw e;
		}
	}

	@Test
	public void onlyCountryIPV4() throws LineParsingException {
		DbIpLineParser dbIpLineParser = new DbIpLineParser();
		String line = "\"1.4.128.0\",\"1.4.255.255\",\"TH\"";
		RawLine rawLine = new RawLine(line, 1);
		ParsedLine parsed = dbIpLineParser.parse(new RawLine(line, 1), false);
		ParsedLine expected = new ParsedLine(
				new IP(InetAddresses.forString("1.4.128.0")),
				new IP(InetAddresses.forString("1.4.255.255")),
				new IPInformation(
						"TH",
						null,
						null,
						null,
						null,
						null,
						new IP(InetAddresses.forString("1.4.128.0")),
						new IP(InetAddresses.forString("1.4.255.255")),
						null
				),
				rawLine);
		Assert.assertEquals(expected, parsed);
	}

	@Test
	public void onlyCountryIPV6() throws LineParsingException {
		DbIpLineParser dbIpLineParser = new DbIpLineParser();
		String line = "\"2a0c:3800:400::\",\"2a0c:3800:400:ffff:ffff:ffff:ffff:ffff\",\"PT\"";
		RawLine rawLine = new RawLine(line, 1);
		ParsedLine parsed = dbIpLineParser.parse(new RawLine(line, 1), false);
		ParsedLine expected = new ParsedLine(
				new IP(InetAddresses.forString("2a0c:3800:400::")),
				new IP(InetAddresses.forString("2a0c:3800:400:ffff:ffff:ffff:ffff:ffff")),
				new IPInformation(
						"PT",
						null,
						null,
						null,
						null,
						null,
						new IP(InetAddresses.forString("2a0c:3800:400::")),
						new IP(InetAddresses.forString("2a0c:3800:400:ffff:ffff:ffff:ffff:ffff")),
						null
				),
				rawLine);
		Assert.assertEquals(expected, parsed);
	}
}