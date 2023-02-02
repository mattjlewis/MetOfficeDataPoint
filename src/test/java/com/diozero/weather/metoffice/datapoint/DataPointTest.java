package com.diozero.weather.metoffice.datapoint;

import java.io.IOException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.TreeMap;

import com.diozero.location.GeographicLocation;
import com.luckycatlabs.sunrisesunset.SunriseSunsetCalculator;
import com.luckycatlabs.sunrisesunset.dto.Location;

public class DataPointTest {
	public static void main(String[] args) {
		if (args.length < 3) {
			System.out.println("Usage: " + DataPointTest.class.getName() + " <api-key> <lat> <long>");
			System.exit(1);
		}

		String api_key = args[0];
		GeographicLocation obs_loc = new GeographicLocation(Float.parseFloat(args[1]), Float.parseFloat(args[2]), 0f,
				"Unknown", "Unknown");

		if (api_key == null) {
			System.out.println("Error, please specify your DataPoint API key");
			System.exit(2);
		}

		DataPoint dp = new DataPoint(api_key);

		try {
			dp.loadSiteList();

			System.out.format("Searching for closest location to %s%n", obs_loc);
			DataPointForecastLocation location = dp.findClosest(obs_loc);
			if (location == null) {
				System.out.println("Location not found");
				return;
			}

			System.out.println("Using DataPoint location: " + location);

			SunriseSunsetCalculator calculator = new SunriseSunsetCalculator(
					new Location(location.getLatitude(), location.getLongitude()), TimeZone.getDefault());

			DataPointForecast f = dp.getForecast(location, DataPoint.Resolution.THREE_HOURLY);
			if (f != null) {
				TreeMap<Long, DataPointReport> forecasts = new TreeMap<>();
				int clear_skies = 0;

				ZonedDateTime current_day = null;
				ZonedDateTime sunset_time = null;
				ZonedDateTime sunrise_time = null;

				for (DataPointReport r : f.getReports()) {
					forecasts.put(Long.valueOf(r.getEpochTime()), r);
					System.out.println(r);

					// A new day?
					if (current_day == null || r.getPeriodStart().isAfter(current_day)) {
						clear_skies = 0;

						// Set sunset to current day's value
						Calendar cal = Calendar.getInstance();
						cal.setTimeInMillis(r.getEpochTime());
						sunset_time = ZonedDateTime.ofInstant(
								calculator.getOfficialSunsetCalendarForDate(cal).toInstant(), ZoneId.systemDefault());

						// Set sunrise to next day's value
						cal.add(Calendar.DAY_OF_MONTH, 1);
						sunrise_time = ZonedDateTime.ofInstant(
								calculator.getOfficialSunriseCalendarForDate(cal).toInstant(), ZoneId.systemDefault());

						System.out.println("Sunset: " + sunset_time + ", Sunrise: " + sunrise_time);

						current_day = r.getPeriodStart();
					}

					System.out.println("report epoch time: " + r.getEpochTime() + ", sunset epoch: "
							+ sunset_time.toInstant().toEpochMilli() + ", sunrise epoch: "
							+ sunrise_time.toInstant().toEpochMilli());
					if (r.getEpochTime() > sunset_time.toInstant().toEpochMilli()
							&& r.getEpochTime() < sunrise_time.toInstant().toEpochMilli()) {
						System.out.println("Nightime forecast, Time: "
								+ DateTimeFormatter.RFC_1123_DATE_TIME.format(new Date(r.getEpochTime()).toInstant())
								+ " [" + r.getMinutesAfterMidnight() + ", Weather type: " + r.getWeatherTypeLabel()
								+ "]");
						if (r.getWeatherTypeValue().equals(DataPointWeatherType._0)) {
							clear_skies++;
							System.out.println("\tClear sky #" + clear_skies + " !!");
						}
					}
				}
			}
		} catch (IOException ioe) {
			System.out.println(ioe);
		}
	}
}
