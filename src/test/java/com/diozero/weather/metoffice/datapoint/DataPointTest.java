package com.diozero.weather.metoffice.datapoint;

import java.io.IOException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.Map.Entry;

import com.luckycatlabs.sunrisesunset.SunriseSunsetCalculator;
import com.luckycatlabs.sunrisesunset.dto.Location;

import gnu.getopt.Getopt;
import gnu.getopt.LongOpt;

public class DataPointTest {
	public static void main(String[] args) {
		Float my_latitude = null;
		Float my_longitude = null;
		String api_key = null;
		Integer site_id = null;
		LongOpt[] long_opts = new LongOpt[] {
			new LongOpt("apikey", LongOpt.REQUIRED_ARGUMENT, null, 'k'),
			new LongOpt("latitude", LongOpt.REQUIRED_ARGUMENT, null, 'a'),
			new LongOpt("longitude", LongOpt.REQUIRED_ARGUMENT, null, 'o'),
			new LongOpt("siteid", LongOpt.REQUIRED_ARGUMENT, null, 's')
		};
		Getopt g = new Getopt("DataPointTest", args, ":k:a:o:s:", long_opts);
		int c;
		String arg;
		while ((c = g.getopt()) != -1) {
			switch (c) {
			case 'k':
				arg = g.getOptarg();
				if (arg == null || arg.isEmpty()) {
					System.out.println("Empty arg value for apikey");
				} else {
					api_key = arg;
				}
				break;
			case 'a':
				arg = g.getOptarg();
				if (arg == null || arg.isEmpty()) {
					System.out.println("Empty arg value for latitude");
				} else {
					my_latitude = Float.valueOf(arg);
				}
				break;
			case 'o':
				arg = g.getOptarg();
				if (arg == null || arg.isEmpty()) {
					System.out.println("Empty arg value for longitude");
				} else {
					my_longitude = Float.valueOf(arg);
				}
				break;
			case 's':
				arg = g.getOptarg();
				if (arg == null || arg.isEmpty()) {
					System.out.println("Empty arg value for siteid");
				} else {
					site_id = Integer.valueOf(arg);
				}
				break;
			case ':':
				System.out.println("You need an argument for option '" + (char)g.getOptopt() + "'");
				break;
			case '?':
				System.out.println("The option '" + (char)g.getOptopt() + "' is not valid");
				break;
			default:
				System.out.println("Unhandled getopt option, getopt() returned: '" + c + "'");
			}
		}
		if (api_key == null) {
			System.out.println("Error, please specify your DataPoint API key");
			System.exit(2);
		}
		
		if (site_id == null && (my_latitude == null || my_longitude == null)) {
			System.out.println("Error, please specify either a siteid or lat/long");
			System.exit(2);
		}
		
		DataPoint dp = new DataPoint(api_key);
		
		try {
			dp.init();
			
			ForecastLocation location = null;
			if (site_id != null) {
				location = dp.getLocation(site_id.intValue());
				if (location == null) {
					System.out.println("Invalid site id '" + site_id + "'");
					return;
				}
			} else if (my_latitude != null && my_longitude != null) {
				System.out.format("Searching for closest location to [%f, %f]%n", my_latitude, my_longitude);
				location = dp.findClosest(my_latitude.floatValue(), my_longitude.floatValue());
			}
			if (location == null) {
				System.out.println("Location not found");
				return;
			}
			
			System.out.println("Using location: " + location);
			
			SunriseSunsetCalculator calculator = new SunriseSunsetCalculator(
					new Location(location.getLatitude(), location.getLongitude()), TimeZone.getDefault());
			
			ZonedDateTime sunset_time = null;
			ZonedDateTime sunrise_time = null;
			
			Forecast f = dp.getForecast(location.getId(), DataPoint.Resolution.THREE_HOURLY);
			if (f != null) {
				TreeMap<ZonedDateTime, Report> forecasts = new TreeMap<>();
				Map<String, Param> params = f.getParams();
				ZonedDateTime start = f.getDataValue().getDataDate();
				String type = f.getDataValue().getType();
				System.out.println(type + " for " + start);
				int clear_skies = 0;
				for (Period period : f.getDataValue().getLocation().getPeriods()) {
					System.out.println(period.getType() + ": " + period.getValue());
					
					if (sunrise_time == null) {
						// Initialise to today's sunrise
						Calendar cal = Calendar.getInstance();
						cal.setTime(Date.from(ZonedDateTime.from(period.getValue()).toInstant()));
						sunrise_time = ZonedDateTime.ofInstant(
								calculator.getOfficialSunriseCalendarForDate(cal).toInstant(), ZoneId.systemDefault());
					}
					if (sunset_time == null) {
						// Initialise to previous day's sunset
						Calendar cal = Calendar.getInstance();
						cal.setTime(Date.from(ZonedDateTime.from(period.getValue()).toInstant()));
						cal.add(Calendar.DAY_OF_MONTH, -1);
						sunset_time = ZonedDateTime.ofInstant(
								calculator.getOfficialSunsetCalendarForDate(cal).toInstant(), ZoneId.systemDefault());
					}
					
					for (Report r : period.getReports()) {
						ZonedDateTime time = r.getReportDateTime();
						forecasts.put(time, r);
						
						// A new day?
						if (time.isAfter(sunrise_time)) {
							clear_skies = 0;
							
							// Set sunset to current day's value
							Calendar cal = Calendar.getInstance();
							cal.setTime(Date.from(time.toInstant()));
							sunset_time = ZonedDateTime.ofInstant(
									calculator.getOfficialSunsetCalendarForDate(cal).toInstant(), ZoneId.systemDefault());
							
							// Set sunrise to next day's value
							cal.add(Calendar.DAY_OF_MONTH, 1);
							sunrise_time = ZonedDateTime.ofInstant(
									calculator.getOfficialSunriseCalendarForDate(cal).toInstant(), ZoneId.systemDefault());
						}
						
						if (time.isAfter(sunset_time) && time.isBefore(sunrise_time)) {
							System.out.println("Nightime forecast, Time: " + DateTimeFormatter.RFC_1123_DATE_TIME.format(time) + " [" + r.getMinutesAfterMidnight()
								+ ", Weather type: " + r.getWeatherType() + " - '" + WeatherTypeMapping.forType(r.getWeatherType()) + "']");
							if (r.getWeatherType().equals("0")) {
								clear_skies++;
								System.out.println("\tClear sky #" + clear_skies + " !!");
								Map<String, Object> values = r.getValues();
								for (Entry<String, Object> entry : values.entrySet()) {
									Param p = params.get(entry.getKey());
									System.out.println("\t" + p.getDescription() + ": " + entry.getValue() + " " + p.getUnits());
								}
							}
						}
					}
				}
			}
		} catch (IOException ioe) {
			System.out.println(ioe);
		}
	}
}
