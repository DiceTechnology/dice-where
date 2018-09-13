package technology.dice.dicewhere.parsing.provider.maxmind;

import org.junit.Assert;
import org.junit.Test;
import technology.dice.dicewhere.reading.provider.maxmind.MaxmindLocation;
import technology.dice.dicewhere.reading.provider.maxmind.MaxmindLocationsParser;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;

public class MaxmindLocationsParserTest {
	@Test
	public void parseCountryDb() {
		String countryDbLines = "102358,en,AS,Asia,SA,\"Saudi Arabia\",0\n" +
				"130758,en,AS,Asia,IR,Iran,0\n" +
				"146669,en,EU,Europe,CY,Cyprus,1\n" +
				"149590,en,AF,Africa,TZ,Tanzania,0\n" +
				"163843,en,AS,Asia,SY,Syria,0";
		InputStream stream = new ByteArrayInputStream(countryDbLines.getBytes());
		BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(stream));
		MaxmindLocationsParser parser = new MaxmindLocationsParser();
		Map<String, MaxmindLocation> locations = parser.locations(bufferedReader);
		Assert.assertEquals(5, locations.keySet().size());
		Assert.assertEquals(new MaxmindLocation("102358", "SA", "", "", ""), locations.get("102358"));
		Assert.assertEquals(new MaxmindLocation("130758", "IR", "", "", ""), locations.get("130758"));
		Assert.assertEquals(new MaxmindLocation("146669", "CY", "", "", ""), locations.get("146669"));
		Assert.assertEquals(new MaxmindLocation("149590", "TZ", "", "", ""), locations.get("149590"));
		Assert.assertEquals(new MaxmindLocation("163843", "SY", "", "", ""), locations.get("163843"));
	}

	@Test
	public void parseCityDb() {
		String countryDbLines = "53654,en,AF,Africa,SO,Somalia,BN,Banaadir,,,Mogadishu,,Africa/Mogadishu,0\n" +
				"256626,en,EU,Europe,GR,Greece,H,\"Central Greece\",04,Euboea,\"Nea Artaki\",,Europe/Athens,1\n" +
				"2633465,en,EU,Europe,GB,\"United Kingdom\",ENG,England,ESX,\"East Sussex\",\"Wych Cross\",,Europe/London,1\n" +
				"482390,en,EU,Europe,RU,Russia,LEN,\"Leningradskaya Oblast'\",,,Tolmachevo,,Europe/Moscow,0\n" +
				"655081,en,EU,Europe,FI,Finland,17,Satakunta,,,Kaasmarkku,,Europe/Helsinki,1\n";
		InputStream stream = new ByteArrayInputStream(countryDbLines.getBytes());
		BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(stream));
		MaxmindLocationsParser parser = new MaxmindLocationsParser();
		Map<String, MaxmindLocation> locations = parser.locations(bufferedReader);
		Assert.assertEquals(5, locations.keySet().size());
		Assert.assertEquals(new MaxmindLocation("53654", "SO", "", "Banaadir", "Mogadishu"), locations.get("53654"));
		Assert.assertEquals(new MaxmindLocation("256626", "GR", "Euboea", "Central Greece", "Nea Artaki"), locations.get("256626"));
		Assert.assertEquals(new MaxmindLocation("2633465", "GB", "East Sussex", "England", "Wych Cross"), locations.get("2633465"));
		Assert.assertEquals(new MaxmindLocation("482390", "RU", "", "Leningradskaya Oblast'", "Tolmachevo"), locations.get("482390"));
		Assert.assertEquals(new MaxmindLocation("655081", "FI", "", "Satakunta", "Kaasmarkku"), locations.get("655081"));
	}
}
