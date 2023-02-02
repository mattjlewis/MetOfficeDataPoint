package com.diozero.satellite;

import java.io.IOException;
import java.net.URL;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.TreeMap;

import org.orekit.errors.OrekitException;
import org.orekit.propagation.analytical.tle.TLE;
import org.orekit.time.AbsoluteDate;
import org.orekit.time.TimeScale;
import org.orekit.time.TimeScalesFactory;

import com.diozero.location.GeographicLocation;
import com.diozero.satellite.orekit.OrekitUtil;
import com.diozero.satellite.orekit.SatelliteUtil;
import com.diozero.satellite.orekit.TleList;
import com.diozero.weather.metoffice.datapoint.DataPoint;
import com.diozero.weather.metoffice.datapoint.DataPointForecast;
import com.luckycatlabs.sunrisesunset.SunriseSunsetCalculator;
import com.luckycatlabs.sunrisesunset.dto.Location;

public class VisibleSatelliteFlybys {
	private static final String SATELLITE_TLE_LIST_URL = "http://celestrak.com/NORAD/elements/visual.txt";

	public static void main(String[] args) {
		if (args.length < 5) {
			System.out.println("Usage: " + VisibleSatelliteFlybys.class.getName()
					+ " <api-key> <lat> <long> <altitude> <city-name> <country-code>");
			System.exit(1);
		}
		int arg = 0;
		String api_key = args[arg++];

		double latitude = Double.parseDouble(args[arg++]);
		double longitude = Double.parseDouble(args[arg++]);
		double altitude = Double.parseDouble(args[arg++]);
		String city_name = args[arg++];
		String country_code = args[arg++];
		GeographicLocation obs_loc = new GeographicLocation(latitude, longitude, altitude, city_name, country_code);

		double min_peak_elev_deg = 45;

		Calendar cal = Calendar.getInstance();
		SunriseSunsetCalculator calculator = new SunriseSunsetCalculator(
				new Location(obs_loc.getLatitude(), obs_loc.getLongitude()), TimeZone.getDefault());
		Date start = calculator.getOfficialSunsetCalendarForDate(cal).getTime();
		cal.add(Calendar.DAY_OF_MONTH, 4);
		Date end = calculator.getOfficialSunriseCalendarForDate(cal).getTime();

		try {
			DataPoint dp = new DataPoint(api_key);

			// Get the 3-hourly weather forecast for this location
			System.out.println("Getting weather forecast for " + obs_loc + "...");
			DataPointForecast forecast = dp.getForecast(obs_loc, DataPoint.Resolution.THREE_HOURLY);

			OrekitUtil.initialise();
			TimeScale utc = TimeScalesFactory.getUTC();

			System.out.println("Loading visual satellite TLE list...");
			TleList tles = new TleList();
			tles.load(new URL(SATELLITE_TLE_LIST_URL));
			System.out.println("Number of tles: " + tles.size());

			System.out.println("Calculating satellite flybys...");
			TreeMap<ZonedDateTime, SatelliteFlyby> sorted_flybys = new TreeMap<>();
			for (Map.Entry<String, TLE> entry : tles.getSatelliteNameMapping().entrySet()) {
				System.out.println("Calculateing visible flybys for " + entry.getKey());
				List<SatelliteFlyby> flybys = SatelliteUtil.calculateVisibleFlybys(entry.getValue(), obs_loc, 10,
						min_peak_elev_deg, new AbsoluteDate(start, utc), new AbsoluteDate(end, utc));
				if (!flybys.isEmpty()) {
					for (SatelliteFlyby flyby : flybys) {
						flyby.setName(entry.getKey());
						sorted_flybys.put(flyby.getPeakDateTime(), flyby);
					}
				}
			}

			System.out.println("Generating output...");
			LinePlotter lp = new LinePlotter(600);
			DateTimeFormatter dt_formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
			System.out.println(
					"Name, Approach Time, Approach Dir, Approach Compass, Peak Time, Peak Elevation, Peak Dir, Peak Compass, Departure Time, Departure Dir, Departure Compass, Weather");
			for (SatelliteFlyby flyby : sorted_flybys.values()) {
				System.out.println(flyby.getName() + ", " + dt_formatter.format(flyby.getApproachDateTime()) + ", "
						+ flyby.getApproachDirectionDeg() + ", " + flyby.getApproachDirectionCompass() + ", "
						+ dt_formatter.format(flyby.getPeakDateTime()) + ", " + flyby.getPeakElevation() + ", "
						+ flyby.getPeakDirectionDeg() + ", " + flyby.getPeakDirectionCompass() + ", "
						+ dt_formatter.format(flyby.getDepartureDateTime()) + ", " + flyby.getDepartureDirectionDeg()
						+ ", " + flyby.getDepartureDirectionCompass() + ", "
						+ forecast.getClosestReport(flyby.getPeakDateTime().toInstant().toEpochMilli())
								.getWeatherTypeValue());
				lp.drawLine(flyby.getApproachDirectionDeg(), flyby.getDepartureDirectionDeg(),
						(int) Math.round(flyby.getPeakElevation() / (90d / 5)));
			}
			lp.writeImage("flybys.jpg");

			System.out.println("Done.");
		} catch (IOException | OrekitException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
