package com.diozero.weather.openweathermap;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

import com.diozero.location.GeographicLocation;

public class OpenWeatherMapTest {
	public static void main(String[] args) throws IOException, InterruptedException {
		if (args.length < 2) {
			System.out.println("Usage: " + OpenWeatherMapTest.class.getName()
					+ " <city-name,[us-state-code],iso3166-country-code> <api-key>");
			System.exit(1);
		}
		String[] location_parts = args[0].split(",");
		if (location_parts.length != 3) {
			System.out.println(
					"Location must be of the format '<city-name,[us-state-code],iso3166-country-code>', e.g. London,,GB");
		}
		String api_key = args[1];

		OpenWeatherMap owm = new OpenWeatherMap(api_key);

		List<GeographicLocation> locations = owm.getLocation(location_parts[0], location_parts[1], location_parts[2]);
		final GeographicLocation location = locations.get(0);

		OwmReport current = owm.getCurrentWeather(locations.get(0));
		System.out.format("Current weather forecast @ %s for %s, %s:%n", new Date(current.getEpochTime()),
				location.getCityName(), location.getCountryCode());
		printReport(current);
		System.out.println();

		OwmForecast forecast = owm.getForecast(locations.get(0));
		System.out.format("Weather forecast @ %s for %s, %s:%n", new Date(forecast.getForecastTime()),
				forecast.getLocation().getCityName(), location.getCountryCode());
		forecast.getReports().forEach(OpenWeatherMapTest::printReport);
	}

	@SuppressWarnings("boxing")
	private static void printReport(OwmReport report) {
		LocalDateTime ldt = Instant.ofEpochMilli(report.getEpochTime()).atZone(ZoneId.systemDefault())
				.toLocalDateTime();
		System.out.format(
				"%s report at %s: %s (%s), %.2f deg C (feels like %.2f), %,d hPa, %d %%rH, %d%% chance of rain, "
						+ "wind %.2fkph gusts to %.2fkph at %d deg, %d%% cloudy, %,dm visibility, moon phase: %.2f, dew point: %.2f%n",
				report.getType(), ldt, report.getWeather().getMain(), report.getWeather().getDescription(),
				report.getTemperature(), report.getFeelsLike(), report.getPressure(), report.getRelativeHumidity(),
				report.getRainProbability(), report.getWindSpeed(), report.getWindGusts(), report.getWindDirection(),
				report.getCloudiness(), report.getVisibility(), report.getMoonPhase(), report.getDewPoint());
	}
}
