package technology.dice.dicewhere.parsing.provider.maxmind;

import com.google.common.collect.ImmutableMap;
import com.google.common.net.InetAddresses;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import technology.dice.dicewhere.api.api.IP;
import technology.dice.dicewhere.api.api.IPInformation;
import technology.dice.dicewhere.api.exceptions.LineParsingException;
import technology.dice.dicewhere.parsing.ParsedLine;
import technology.dice.dicewhere.reading.RawLine;
import technology.dice.dicewhere.reading.provider.maxmind.MaxmindLocation;

import java.util.Map;

public class MaxmindLineParserTest {

	private static Map<String, MaxmindLocation> locationNames;

	@BeforeClass
	static public void beforeClass() {
		locationNames = ImmutableMap.<String, MaxmindLocation>builder()
				.put("2634096", new MaxmindLocation("2634096", "GB", "Cumbria", "England", "Whitehaven"))
				.put("3372745", new MaxmindLocation("3372745", "PT", "", "Azores", "Rabo De Peixe"))
				.build();
	}

	@Test
	public void ipV4LineWithOriginalLine() throws LineParsingException {
		MaxmindLineParser maxmindLineParser = new MaxmindLineParser(locationNames);
		String line = "78.29.134.0/25,3372745,2264397,,0,0,9600-082,37.8000,-25.5833,500";
		RawLine rawLine = new RawLine(line, 1);
		ParsedLine parsed = maxmindLineParser.parse(rawLine, true);
		ParsedLine expected = new ParsedLine(
				new IP(InetAddresses.forString("78.29.134.0")),
				new IP(InetAddresses.forString("78.29.134.127")),
				new IPInformation(
						"PT",
						"3372745",
						"Rabo De Peixe",
						"Azores",
						"",
						"9600-082",
						new IP(InetAddresses.forString("78.29.134.0")),
						new IP(InetAddresses.forString("78.29.134.127")),
						line
				),
				rawLine
		);
		Assert.assertEquals(expected, parsed);
	}

	@Test
	public void ipV4LineWithoutOriginalLine() throws LineParsingException {
		MaxmindLineParser maxmindLineParser = new MaxmindLineParser(locationNames);
		String line = "78.29.134.0/25,3372745,2264397,,0,0,9600-082,37.8000,-25.5833,500";
		RawLine rawLine = new RawLine(line, 1);
		ParsedLine parsed = maxmindLineParser.parse(new RawLine(line, 1), false);
		ParsedLine expected = new ParsedLine(
				new IP(InetAddresses.forString("78.29.134.0")),
				new IP(InetAddresses.forString("78.29.134.127")),
				new IPInformation(
						"PT",
						"3372745",
						"Rabo De Peixe",
						"Azores",
						"",
						"9600-082",
						new IP(InetAddresses.forString("78.29.134.0")),
						new IP(InetAddresses.forString("78.29.134.127")),
						null
				),
				rawLine);
		Assert.assertEquals(expected, parsed);
	}

	@Test
	public void ipV6LineWithOriginal() throws LineParsingException {
		MaxmindLineParser maxmindParser = new MaxmindLineParser(locationNames);
		String line = "2a02:c7f:6a02::/47,2634096,2635167,,0,0,CA28,54.5578,-3.5837,10";
		RawLine rawLine = new RawLine(line, 1);
		ParsedLine parsed = maxmindParser.parse(new RawLine(line, 1), true);
		ParsedLine expected = new ParsedLine(
				new IP(InetAddresses.forString("2a02:c7f:6a02:0:0:0:0:0")),
				new IP(InetAddresses.forString("2a02:c7f:6a03:ffff:ffff:ffff:ffff:ffff")),
				new IPInformation(
						"GB",
						"2634096",
						"Whitehaven",
						"England",
						"Cumbria",
						"CA28",
						new IP(InetAddresses.forString("2a02:c7f:6a02:0:0:0:0:0")),
						new IP(InetAddresses.forString("2a02:c7f:6a03:ffff:ffff:ffff:ffff:ffff")),
						line
				),
				rawLine);
		Assert.assertEquals(expected, parsed);
	}

	@Test
	public void locationMissing() throws LineParsingException {
		MaxmindLineParser maxmindParser = new MaxmindLineParser(locationNames);
		String line = "::/0,5,5,,0,0,CA28,54.5578,-3.5837,10";
		RawLine rawLine = new RawLine(line, 1);
		ParsedLine parsed = maxmindParser.parse(new RawLine(line, 1), true);
		ParsedLine expected = new ParsedLine(
				new IP(InetAddresses.forString("0:0:0:0:0:0:0:0")),
				new IP(InetAddresses.forString("ffff:ffff:ffff:ffff:ffff:ffff:ffff:ffff")),
				new IPInformation(
						"ZZ",
						"5",
						"",
						"",
						"",
						"CA28",
						new IP(InetAddresses.forString("0:0:0:0:0:0:0:0")),
						new IP(InetAddresses.forString("ffff:ffff:ffff:ffff:ffff:ffff:ffff:ffff")),
						line
				),
				rawLine);
		Assert.assertEquals(expected, parsed);
	}

	@Test
	public void ipV6LineWithoutOriginal() throws LineParsingException {
		MaxmindLineParser maxmindParser = new MaxmindLineParser(locationNames);
		String line = "2a02:c7f:6a02::/47,2634096,2635167,,0,0,CA28,54.5578,-3.5837,10";
		RawLine rawLine = new RawLine(line, 1);
		ParsedLine parsed = maxmindParser.parse(new RawLine(line, 1), false);
		ParsedLine expected = new ParsedLine(
				new IP(InetAddresses.forString("2a02:c7f:6a02:0:0:0:0:0")),
				new IP(InetAddresses.forString("2a02:c7f:6a03:ffff:ffff:ffff:ffff:ffff")),
				new IPInformation(
						"GB",
						"2634096",
						"Whitehaven",
						"England",
						"Cumbria",
						"CA28",
						new IP(InetAddresses.forString("2a02:c7f:6a02:0:0:0:0:0")),
						new IP(InetAddresses.forString("2a02:c7f:6a03:ffff:ffff:ffff:ffff:ffff")),
						null
				),
				rawLine);
		Assert.assertEquals(expected, parsed);
	}


	@Test(expected = LineParsingException.class)
	public void wrongLineFormat() throws LineParsingException {
		MaxmindLineParser maxmindParser = new MaxmindLineParser(locationNames);
		String line = "column1,column2,column3";
		try {
			maxmindParser.parse(new RawLine(line, 1), false);
		} catch (LineParsingException e) {
			Assert.assertEquals(line, e.getOffendingLine().getLine());
			Assert.assertEquals(1, e.getOffendingLine().getLineNumber());
			throw e;
		}
	}

	@Test(expected = LineParsingException.class)
	public void wrongCIDRFormat() throws LineParsingException {
		MaxmindLineParser maxmindParser = new MaxmindLineParser(locationNames);
		String line = "2a02:c7f:6a02:Nop:/47,2634096,2635167,,0,0,CA28,54.5578,-3.5837,10";
		try {
			maxmindParser.parse(new RawLine(line, 1), false);
		} catch (LineParsingException e) {
			Assert.assertEquals(line, e.getOffendingLine().getLine());
			Assert.assertEquals(1, e.getOffendingLine().getLineNumber());
			throw e;
		}
	}

	@Test(expected = LineParsingException.class)
	public void impossibleCIDR() throws LineParsingException {
		MaxmindLineParser maxmindParser = new MaxmindLineParser(locationNames);
		String line = "2a02:c7f:6a02::/1000,2634096,2635167,,0,0,CA28,54.5578,-3.5837,10";
		try {
			maxmindParser.parse(new RawLine(line, 1), false);
		} catch (LineParsingException e) {
			Assert.assertEquals(line, e.getOffendingLine().getLine());
			Assert.assertEquals(1, e.getOffendingLine().getLineNumber());
			throw e;
		}
	}
}