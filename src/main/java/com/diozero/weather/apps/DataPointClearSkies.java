package com.diozero.weather.apps;

import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import com.diozero.location.GeographicLocation;
import com.diozero.weather.metoffice.datapoint.DataPoint;
import com.diozero.weather.metoffice.datapoint.DpForecast;
import com.diozero.weather.metoffice.datapoint.DpForecastLocation;
import com.diozero.weather.metoffice.datapoint.DpReport;
import com.diozero.weather.metoffice.datapoint.DpWeatherType;
import com.luckycatlabs.sunrisesunset.SunriseSunsetCalculator;
import com.luckycatlabs.sunrisesunset.dto.Location;

public class DataPointClearSkies {
	public static void main(String[] args) {
		if (args.length < 3) {
			System.out.println("Usage: " + DataPointClearSkies.class.getName() + " <api-key> <lat> <long>");
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
			DpForecastLocation location = dp.findClosest(obs_loc);
			if (location == null) {
				System.out.println("Location not found");
				return;
			}

			System.out.println("Using DataPoint location: " + location);

			SunriseSunsetCalculator calculator = new SunriseSunsetCalculator(
					new Location(location.getLatitude(), location.getLongitude()), TimeZone.getDefault());

			DpForecast f = dp.getForecast(location, DataPoint.Resolution.THREE_HOURLY);
			if (f != null) {
				int clear_skies = 0;

				ZonedDateTime sunset_time = null;
				ZonedDateTime sunrise_time = null;

				for (DpReport r : f.getReports()) {
					ZonedDateTime report_zdt = Instant.ofEpochMilli(r.getEpochTime()).atZone(ZoneId.systemDefault());

					if (sunrise_time == null) {
						System.out.println("\tInitialising sunrise and sunset for period start: " + r.getPeriodStart());
						Calendar cal = GregorianCalendar.from(r.getPeriodStart());
						// Calculate sunrise for the current day
						sunrise_time = ZonedDateTime.ofInstant(
								calculator.getOfficialSunriseCalendarForDate(cal).toInstant(), ZoneId.systemDefault());
						// Calculate sunset for the previous day
						cal.add(Calendar.DAY_OF_MONTH, -1);
						sunset_time = ZonedDateTime.ofInstant(
								calculator.getOfficialSunsetCalendarForDate(cal).toInstant(), ZoneId.systemDefault());
					}

					if (report_zdt.isAfter(sunrise_time)) {
						System.out.println("\tReport date/time (" + report_zdt
								+ ") is after sunrise - resetting sunrise, sunset and clear skies counter!, "
								+ r.getPeriodStart());
						clear_skies = 0;

						Calendar cal = GregorianCalendar.from(report_zdt);
						// Calculate sunset for the current day
						sunset_time = ZonedDateTime.ofInstant(
								calculator.getOfficialSunsetCalendarForDate(cal).toInstant(), ZoneId.systemDefault());
						// Calculate sunrise for the next day
						cal.add(Calendar.DAY_OF_MONTH, 1);
						sunrise_time = ZonedDateTime.ofInstant(
								calculator.getOfficialSunriseCalendarForDate(cal).toInstant(), ZoneId.systemDefault());

						System.out.println("\tSunset: " + DateTimeFormatter.RFC_1123_DATE_TIME.format(sunset_time)
								+ ", Sunrise: " + DateTimeFormatter.RFC_1123_DATE_TIME.format(sunrise_time));
					}

					if (report_zdt.isAfter(sunset_time) && report_zdt.isBefore(sunrise_time)) {
						System.out.println("Nighttime forecast (" + report_zdt + "): [" + r.getMinutesAfterMidnight()
								+ ", Weather type: " + r.getWeatherTypeLabel() + "] - " + r);
						if (r.getWeatherTypeValue().equals(DpWeatherType._0)) {
							clear_skies++;
							System.out.println("\tClear sky #" + clear_skies + " !!");
						}
					} else {
						System.out.println("Daytime forecast report (" + report_zdt + "): " + r);
					}
				}
			}
		} catch (IOException ioe) {
			System.out.println(ioe);
		}
	}
}
