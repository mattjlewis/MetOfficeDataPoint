package com.diozero.satellite;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import org.orekit.errors.OrekitException;
import org.orekit.propagation.analytical.tle.TLE;
import org.orekit.time.AbsoluteDate;
import org.orekit.time.TimeScale;
import org.orekit.time.TimeScalesFactory;

import com.diozero.satellite.orekit.OrekitUtil;
import com.diozero.satellite.orekit.SatelliteUtil;
import com.diozero.satellite.orekit.TleList;
import com.diozero.weather.metoffice.datapoint.*;
import com.luckycatlabs.sunrisesunset.SunriseSunsetCalculator;
import com.luckycatlabs.sunrisesunset.dto.Location;

public class VisibleSatelliteFlybys {
	public static void main(String[] args) throws MalformedURLException {
		if (args.length < 5) {
			System.out.println("Usage: " + VisibleSatelliteFlybys.class.getName() + " <api-key> <lat> <long> <alt> <name>");
			System.exit(1);
		}
		int arg = 0;
		String api_key = args[arg++];
		
		double latitude = Double.parseDouble(args[arg++]);
		double longitude = Double.parseDouble(args[arg++]);
		double altitude = Double.parseDouble(args[arg++]);
		String name = args[arg++];
		ObservationLocation obs_loc = new ObservationLocation(latitude, longitude, altitude, name);
		
		double min_peak_elev_deg = 45;
		
		//String tle_list = "tle/visual.txt";
		URL tle_list = new URL("http://celestrak.com/NORAD/elements/visual.txt");
		
		DataPoint dp = new DataPoint(api_key);
		
		Calendar cal = Calendar.getInstance();
		SunriseSunsetCalculator calculator = new SunriseSunsetCalculator(
				new Location(obs_loc.getLatitude(), obs_loc.getLongitude()), TimeZone.getDefault());
		Date start = calculator.getOfficialSunsetCalendarForDate(cal).getTime();
		cal.add(Calendar.DAY_OF_MONTH, 4);
		Date end = calculator.getOfficialSunriseCalendarForDate(cal).getTime();
		
		try {
			// Load weather forecast
			dp.init();
			ForecastLocation location = dp.findClosest(obs_loc.getLatitude(), obs_loc.getLongitude());
			Forecast forecast = dp.getForecast(location.getId(), DataPoint.Resolution.THREE_HOURLY);
			
			OrekitUtil.initialise();
			TimeScale utc = TimeScalesFactory.getUTC();
			
			//AbsoluteDate start_date = new AbsoluteDate(new Date(), utc);
			//AbsoluteDate start_date = new AbsoluteDate(2016, 8, 5, 19, 0, 0, utc);
			AbsoluteDate start_date = new AbsoluteDate(start, utc);
			
			//Calendar cal = Calendar.getInstance();
			//cal.add(Calendar.DAY_OF_MONTH, 7);
			//AbsoluteDate end_date = new AbsoluteDate(cal.getTime(), utc);
			//AbsoluteDate end_date = new AbsoluteDate(2016, 8, 6, 23, 59, 59, utc);
			AbsoluteDate end_date = new AbsoluteDate(end, utc);
			
			TleList tles = new TleList();
			tles.load(tle_list);
			
			TreeMap<ZonedDateTime, SatelliteFlyby> sorted_flybys = new TreeMap<>();
			for (Map.Entry<String, TLE> entry : tles.getSatelliteNameMapping().entrySet()) {
				List<SatelliteFlyby> flybys = SatelliteUtil.calculateVisibleFlybys(
						entry.getValue(), obs_loc, 10, min_peak_elev_deg , start_date, end_date);
				if (! flybys.isEmpty()) {
					for (SatelliteFlyby flyby : flybys) {
						flyby.setName(entry.getKey());
						sorted_flybys.put(flyby.getPeakDateTime(), flyby);
					}
				}
			}

			LinePlotter lp = new LinePlotter(600);
			DateTimeFormatter dt_formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
			System.out.println("Name, Approach Time, Approach Dir, Approach Compass, Peak Time, Peak Elevation, Peak Dir, Peak Compass, Departure Time, Departure Dir, Departure Compass, Weather");
			for (SatelliteFlyby flyby : sorted_flybys.values()) {
				System.out.println(flyby.getName()
						+ ", " + dt_formatter.format(flyby.getApproachDateTime()) + ", " + flyby.getApproachDirectionDeg() + ", " + flyby.getApproachDirectionCompass()
						+ ", " + dt_formatter.format(flyby.getPeakDateTime()) + ", " + flyby.getPeakElevation() + ", " + flyby.getPeakDirectionDeg() + ", " + flyby.getPeakDirectionCompass()
						+ ", " + dt_formatter.format(flyby.getDepartureDateTime()) + ", " + flyby.getDepartureDirectionDeg() + ", " + flyby.getDepartureDirectionCompass()
						+ ", " + WeatherTypeMapping.forType(forecast.getClosestReport(flyby.getPeakDateTime()).getWeatherType())
						);
				lp.drawLine(flyby.getApproachDirectionDeg(), flyby.getDepartureDirectionDeg(), (int) Math.round(flyby.getPeakElevation() / (90d/5)));
			}
			lp.writeImage("flybys.jpg");
		} catch (IOException | OrekitException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
