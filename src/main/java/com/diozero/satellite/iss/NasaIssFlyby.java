package com.diozero.satellite.iss;

import java.io.IOException;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;

import com.diozero.satellite.SatelliteFlyby;

public class NasaIssFlyby {
	private static final String DATE_GROUP_ID = "date";
	private static final String TIME_GROUP_ID = "time";
	private static final String DURATION_GROUP_ID = "duration";
	private static final String MAX_ELEV_GROUP_ID = "maxElev";
	private static final String APPROACH_ANGLE_GROUP_ID = "approachAngle";
	private static final String APPROACH_ELEV_GROUP_ID = "approachElev";
	private static final String DEPARTURE_ANGLE_GROUP_ID = "departureAngle";
	private static final String DEPARTURE_ELEV_GROUP_ID = "departureElev";
	private static final String DESCRIPTION_REGEX = String.format("\\s*Date: (?<%s>.*) <br/>"
				+ "\\s*Time: (?<%s>.*) <br/>"
				+ "\\s*Duration: (less than)?\\s*(?<%s>\\d*) minutes? <br/>"
				+ "\\s*Maximum Elevation: (?<%s>\\d*)° <br/>"
				+ "\\s*Approach: (?<%s>\\d*)° above (?<%s>\\w*) <br/>"
				+ "\\s*Departure: (?<%s>\\d*)° above (?<%s>\\w*) <br/>.*",
				DATE_GROUP_ID, TIME_GROUP_ID, DURATION_GROUP_ID, MAX_ELEV_GROUP_ID,
				APPROACH_ANGLE_GROUP_ID, APPROACH_ELEV_GROUP_ID, DEPARTURE_ANGLE_GROUP_ID,
				DEPARTURE_ELEV_GROUP_ID);
	private static final String URI_FORMAT = "https://spotthestation.nasa.gov/sightings/xml_files/%s.xml";
	private static final String DATE_FORMAT = "EEEE MMM d, yyyy";
	private static final String TIME_FORMAT = "K:mm a";
	private static final String LOCATION_FORMAT = "%s_%s_%s";

	public static void main(String[] args) {
		try {
			List<SatelliteFlyby> iss_passes = getFlybys("United Kingdom", "England", "Farnborough", ZoneId.of("Europe/London"));
			for (SatelliteFlyby iss_pass : iss_passes) {
				System.out.println(iss_pass);
			}
			
			iss_passes = getFlybys("Afghanistan", null, "Kabul", ZoneId.of("Asia/Kabul"));
			for (SatelliteFlyby iss_pass : iss_passes) {
				System.out.println(iss_pass);
			}
		} catch (IOException e) {
			System.out.println("Error: " + e);
			e.printStackTrace();
		}
	}
	
	/**
	 * Based on the <a href="https://spotthestation.nasa.gov/sightings/index.cfm">NASA Sighting Opportunities RSS Feed</a>.
	 * Visit that page for valid values for country / region / city.
	 * @param country Country, e.g. United Kingdom
	 * @param region Region, e.g. England
	 * @param city City, e.g. London
	 * @param zoneId Date / time Zone Id for the specified location
	 * @return
	 */
	public static List<SatelliteFlyby> getFlybys(String country, String region, String city, ZoneId zoneId) throws IOException {
		String location = String.format(LOCATION_FORMAT, country.replace(" ", "_"), region == null ? "None" : region, city);
		
		Pattern regex = Pattern.compile(DESCRIPTION_REGEX, Pattern.MULTILINE);
		
		DateTimeFormatter date_formatter = DateTimeFormatter.ofPattern(DATE_FORMAT);
		DateTimeFormatter time_formatter = DateTimeFormatter.ofPattern(TIME_FORMAT);
		
		List<SatelliteFlyby> iss_passes = new ArrayList<>();
		try {
			Document doc = new SAXBuilder().build(String.format(URI_FORMAT, location));
			Element rss = doc.getRootElement();
			Element channel = rss.getChild("channel");
			List<Element> items = channel.getChildren("item");
			for (Element item : items) {
				String desc = item.getChildText("description");
				Matcher m = regex.matcher(desc);
				if (! m.find()) {
					throw new IOException("Error, the returned description element text didn't match the regular expression");
				}
				
				LocalDate date = date_formatter.parse(m.group(DATE_GROUP_ID), LocalDate::from);
				LocalTime time = time_formatter.parse(m.group(TIME_GROUP_ID), LocalTime::from);
				ZonedDateTime approach_date_time = ZonedDateTime.of(date, time, zoneId);
				int duration = Integer.parseInt(m.group(DURATION_GROUP_ID));
				int max_elev = Integer.parseInt(m.group(MAX_ELEV_GROUP_ID));
				int approach_elev = Integer.parseInt(m.group(APPROACH_ANGLE_GROUP_ID));
				String approach_angle = m.group(APPROACH_ELEV_GROUP_ID);
				ZonedDateTime departure_date_time = approach_date_time.plusMinutes(duration);
				int departure_elev = Integer.parseInt(m.group(DEPARTURE_ANGLE_GROUP_ID));
				String departure_angle = m.group(DEPARTURE_ELEV_GROUP_ID);
				
				iss_passes.add(new SatelliteFlyby(approach_date_time, approach_elev, approach_angle,
						max_elev,
						departure_date_time, departure_elev, departure_angle));
			}
		} catch (JDOMException e) {
			throw new IOException(e);
		}
		
		return iss_passes;
	}
}
