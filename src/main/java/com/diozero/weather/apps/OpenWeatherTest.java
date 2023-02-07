package com.diozero.weather.apps;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

import com.diozero.location.GeographicLocation;
import com.diozero.weather.openweather.OpenWeather;
import com.diozero.weather.openweather.OwForecast;
import com.diozero.weather.openweather.OwReport;

public class OpenWeatherTest {
	public static void main(String[] args) throws IOException, InterruptedException {
		if (args.length < 2) {
			System.out.println("Usage: " + OpenWeatherTest.class.getName()
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

		OwReport current = owm.getCurrentWeather(locations.get(0));
		System.out.format("Current weather forecast @ %s for %s, %s:%n", new Date(current.getEpochTime()),
				location.getCityName(), location.getCountryCode());
		printReport(current);
		System.out.println();

		OwForecast forecast = owm.getForecast(locations.get(0));
		System.out.format("Weather forecast @ %s for %s, %s:%n", new Date(forecast.getForecastTime()),
				forecast.getLocation().getCityName(), location.getCountryCode());
		forecast.getReports().forEach(OpenWeatherTest::printReport);
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
