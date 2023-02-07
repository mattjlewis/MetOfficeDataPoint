package com.diozero.weather.apps;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;

import com.diozero.location.GeographicLocation;
import com.diozero.weather.openweather.OpenWeather;
import com.diozero.weather.openweather.OwForecast;
import com.diozero.weather.openweather.OwReport;
import com.diozero.weather.openweather.OwWeatherType;
import com.luckycatlabs.sunrisesunset.SunriseSunsetCalculator;
import com.luckycatlabs.sunrisesunset.dto.Location;

public class OpenWeatherClearSkies {
	public static void main(String[] args) throws IOException, InterruptedException {
		if (args.length < 2) {
			System.out.println("Usage: " + OpenWeatherClearSkies.class.getName()
					+ " <city-name,[us-state-code],iso3166-country-code> <api-key>");
			System.exit(1);
		}
		String[] location_parts = args[0].split(",");
		if (location_parts.length != 3) {
			System.out.println(
					"Location must be of the format '<city-name,[us-state-code],iso3166-country-code>', e.g. London,,GB");
		}
		String api_key = args[1];

		OpenWeather owm = new OpenWeather(api_key);

		System.out.println("Getting location for " + String.join(",", location_parts));
		List<GeographicLocation> locations = owm.getLocation(location_parts[0], location_parts[1], location_parts[2]);
		final GeographicLocation location = locations.get(0);
		System.out.println("Using location " + location);

		SunriseSunsetCalculator calculator = new SunriseSunsetCalculator(
				new Location(location.getLatitude(), location.getLongitude()), TimeZone.getDefault());

		OwReport current = owm.getCurrentWeather(locations.get(0));
		System.out.format("Current weather forecast @ %s for %s, %s:%n", new Date(current.getEpochTime()),
				location.getCityName(), location.getCountryCode());
		printReport(current);
		System.out.println();

		OwForecast forecast = owm.get5DayForecast(locations.get(0));
		System.out.format("5-day weather forecast @ %s for %s, %s:%n", new Date(forecast.getForecastTime()),
				forecast.getLocation().getCityName(), location.getCountryCode());
		forecast.getReports().forEach(OpenWeatherClearSkies::printReport);

		int clear_skies = 0;

		ZonedDateTime sunset_time = null;
		ZonedDateTime sunrise_time = null;

		for (OwReport r : forecast.getReports()) {
			ZonedDateTime report_zdt = Instant.ofEpochMilli(r.getEpochTime()).atZone(ZoneId.systemDefault());

			if (sunrise_time == null) {
				System.out.println("\tInitialising sunrise and sunset for initial report data/time: " + report_zdt);
				Calendar cal = GregorianCalendar.from(report_zdt);
				// Calculate sunrise for the current day
				sunrise_time = ZonedDateTime.ofInstant(calculator.getOfficialSunriseCalendarForDate(cal).toInstant(),
						ZoneId.systemDefault());
				// Calculate sunset for the previous day
				cal.add(Calendar.DAY_OF_MONTH, -1);
				sunset_time = ZonedDateTime.ofInstant(calculator.getOfficialSunsetCalendarForDate(cal).toInstant(),
						ZoneId.systemDefault());
			}

			if (report_zdt.isAfter(sunrise_time)) {
				System.out.println("\tReport date/time (" + report_zdt
						+ ") is after sunrise - resetting sunrise, sunset and clear skies counter!, " + report_zdt);
				clear_skies = 0;

				Calendar cal = GregorianCalendar.from(report_zdt);
				// Calculate sunset for the current day
				sunset_time = ZonedDateTime.ofInstant(calculator.getOfficialSunsetCalendarForDate(cal).toInstant(),
						ZoneId.systemDefault());
				// Calculate sunrise for the next day
				cal.add(Calendar.DAY_OF_MONTH, 1);
				sunrise_time = ZonedDateTime.ofInstant(calculator.getOfficialSunriseCalendarForDate(cal).toInstant(),
						ZoneId.systemDefault());

				System.out.println("\tSunset: " + DateTimeFormatter.RFC_1123_DATE_TIME.format(sunset_time)
						+ ", Sunrise: " + DateTimeFormatter.RFC_1123_DATE_TIME.format(sunrise_time));
			}

			if (report_zdt.isAfter(sunset_time) && report_zdt.isBefore(sunrise_time)) {
				System.out.println(
						"Nighttime forecast (" + report_zdt + "): [Weather type: " + r.getWeatherType() + "] - " + r);
				if (r.getWeatherType().getId() == OwWeatherType.CLEAR_SKY) {
					clear_skies++;
					System.out.println("\tClear sky #" + clear_skies + " !!");
					if (!r.getWeatherType().getIcon().equals(OwWeatherType.CLEAR_SKY_NIGHT_ICON)) {
						System.out.println("*** After sunset but icon is not night - " + r.getWeatherType().getIcon());
					}
				}
			} else {
				System.out.println("Daytime forecast report (" + report_zdt + "): " + r);
			}
		}
	}

	@SuppressWarnings("boxing")
	private static void printReport(OwReport report) {
		LocalDateTime ldt = Instant.ofEpochMilli(report.getEpochTime()).atZone(ZoneId.systemDefault())
				.toLocalDateTime();
		System.out.format(
				"%s report at %s: %s (%s), %.2f deg C (feels like %.2f), %,d hPa, %d %%rH, %d%% chance of rain, "
						+ "wind %.2fkph gusts to %.2fkph at %d deg, %d%% cloudy, %,dm visibility, moon phase: %.2f, dew point: %.2f%n",
				report.getType(), ldt, report.getWeatherType().getMain(), report.getWeatherType().getDescription(),
				report.getTemperature(), report.getFeelsLike(), report.getPressure(), report.getRelativeHumidity(),
				report.getRainProbability(), report.getWindSpeed(), report.getWindGusts(), report.getWindDirection(),
				report.getCloudiness(), report.getVisibility(), report.getMoonPhase(), report.getDewPoint());
	}
}
