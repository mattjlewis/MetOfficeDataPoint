package com.diozero.weather.openweather;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.diozero.location.GeographicLocation;

import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonNumber;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import jakarta.json.JsonValue;
import jakarta.json.JsonValue.ValueType;

public class OpenWeather {
	private String apiKey;
	private HttpClient httpClient;
	private String rootWeatherUri;

	public OpenWeather(String apiKey) {
		this.apiKey = apiKey;

		httpClient = HttpClient.newHttpClient();
		rootWeatherUri = "https://api.openweathermap.org/data/2.5";
	}

	public List<GeographicLocation> getLocation(String cityName, String stateCode, String countryCode)
			throws IOException, InterruptedException {
		// https://api.openweathermap.org/geo/1.0/direct?q={city name},{state
		// code},{country code}&limit={limit}&appid={API key}
		HttpRequest request = HttpRequest.newBuilder()
				.uri(URI.create("https://api.openweathermap.org/geo/1.0/direct?q="
						+ String.join(",", cityName, stateCode, countryCode) + "&appid=" + apiKey))
				.header("Content-Type", "application/json").build();
		HttpResponse<InputStream> response = httpClient.send(request, BodyHandlers.ofInputStream());
		if (response.statusCode() != 200) {
			System.out.format("Invalid response to request '%s': %d%n", request.uri(),
					Integer.valueOf(response.statusCode()));
			throw new IllegalStateException(String.format("Invalid response to request '%s': %d%n", request.uri(),
					Integer.valueOf(response.statusCode())));
		}

		try (InputStream is = response.body(); JsonReader reader = Json.createReader(is)) {
			return reader.readArray().stream().map(jv -> jv.asJsonObject()).map(OpenWeather::parseLocation)
					.collect(Collectors.toList());
		}
	}

	private static GeographicLocation parseLocation(JsonObject location) {
		/*-
		 * {
		 *  "name": "London",
		 *   "local_names": {
		 *   "af": "Londen"
		 *  },
		 *  "lat": 51.5085,
		 *  "lon": -0.1257,
		 *  "country": "GB"
		 * }
		 */
		return new GeographicLocation(location.getJsonNumber("lat").doubleValue(),
				location.getJsonNumber("lon").doubleValue(), 0, location.getString("name"),
				location.getString("country"));
	}

	public OwReport getCurrentWeather(GeographicLocation location) throws IOException, InterruptedException {
		// https://api.openweathermap.org/data/2.5/weather?lat=51.1&lon=-0.73&appid=xxx
		HttpRequest request = HttpRequest.newBuilder()
				.uri(URI.create(String.format("%s/weather?lat=%f&lon=%f&units=metric&appid=%s", rootWeatherUri,
						Double.valueOf(location.getLatitude()), Double.valueOf(location.getLongitude()), apiKey)))
				.header("Content-Type", "application/json").build();

		HttpResponse<InputStream> response = httpClient.send(request, BodyHandlers.ofInputStream());
		if (response.statusCode() != 200) {
			System.out.format("Invalid response to request '%s': %d%n", request.uri(),
					Integer.valueOf(response.statusCode()));
			throw new IllegalStateException(String.format("Invalid response to request '%s': %d%n", request.uri(),
					Integer.valueOf(response.statusCode())));
		}

		try (InputStream is = response.body(); JsonReader reader = Json.createReader(is)) {
			/*-
			 * {"coord":{"lon":-0.73,"lat":51.1},
			 * "weather":[
			 * {"id":803,"main":"Clouds","description":"broken clouds","icon":"04d"}],
			 * "base":"stations",
			 * "main":{"temp":282.52,"feels_like":279.9,"temp_min":281.48,"temp_max":284,"pressure":1028,"humidity":85},
			 * "visibility":10000,"wind":{"speed":5.14,"deg":250},
			 * "clouds":{"all":75},"dt":1675347403,
			 * "sys":{"type":2,"id":2078889,"country":"GB","sunrise":1675323544,"sunset":1675356840},
			 * "timezone":0,"id":2646863,"name":"Hindhead","cod":200}
			 */
			return parseForecastReport(OwReport.Type.CURRENT, reader.readObject());
		}
	}

	/*
	 * Note that version 3.0 requires addition of payment details, for
	 * "Pay as you call" with 1,000 API calls per day for free 0.0012 GBP per API
	 * call over the daily limit.
	 */
	public OwForecast getForecast(GeographicLocation location) throws IOException, InterruptedException {
		// https://api.openweathermap.org/data/2.5/onecall?lat=51.1&lon=-0.73
		// &exclude=current,minutely,hourly,daily,alerts
		// &appid=xxx
		HttpRequest request = HttpRequest.newBuilder()
				.uri(URI.create(String.format("%s/onecall?lat=%f&lon=%f&exclude=minutely&units=metric&appid=%s",
						rootWeatherUri, Double.valueOf(location.getLatitude()), Double.valueOf(location.getLongitude()),
						apiKey)))
				.header("Content-Type", "application/json").build();

		HttpResponse<InputStream> response = httpClient.send(request, BodyHandlers.ofInputStream());
		if (response.statusCode() != 200) {
			System.out.format("Invalid response to request '%s': %d%n", request.uri(),
					Integer.valueOf(response.statusCode()));
			throw new IllegalStateException(String.format("Invalid response to request '%s': %d%n", request.uri(),
					Integer.valueOf(response.statusCode())));
		}

		final List<OwReport> reports = new ArrayList<>();
		try (InputStream is = response.body(); JsonReader reader = Json.createReader(is)) {
			JsonObject root = reader.readObject();

			JsonObject obj = root.getJsonObject("current");
			if (obj != null) {
				reports.add(parseForecastReport(OwReport.Type.CURRENT, obj));
			}

			JsonArray array = root.getJsonArray("minutely");
			if (array != null) {
				reports.addAll(
						array.stream().map(value -> parseForecastReport(OwReport.Type.MINUTELY, (JsonObject) value))
								.collect(Collectors.toList()));
			}

			array = root.getJsonArray("hourly");
			if (array != null) {
				reports.addAll(
						array.stream().map(value -> parseForecastReport(OwReport.Type.HOURLY, (JsonObject) value))
								.collect(Collectors.toList()));
			}

			array = root.getJsonArray("daily");
			if (array != null) {
				reports.addAll(array.stream().map(value -> parseForecastReport(OwReport.Type.DAILY, (JsonObject) value))
						.collect(Collectors.toList()));
			}
		}

		return new OwForecast(location, reports);
	}

	public OwForecast get5DayForecast(GeographicLocation location) throws IOException, InterruptedException {
		// https://api.openweathermap.org/data/2.5/forecast?lat=51.1&lon=-0.73&appid=xxx
		HttpRequest request = HttpRequest.newBuilder()
				.uri(URI.create(String.format("%s/forecast?lat=%f&lon=%f&units=metric&appid=%s", rootWeatherUri,
						Double.valueOf(location.getLatitude()), Double.valueOf(location.getLongitude()), apiKey)))
				.header("Content-Type", "application/json").build();

		HttpResponse<InputStream> response = httpClient.send(request, BodyHandlers.ofInputStream());
		if (response.statusCode() != 200) {
			System.out.format("Invalid response to request '%s': %d%n", request.uri(),
					Integer.valueOf(response.statusCode()));
			throw new IllegalStateException(String.format("Invalid response to request '%s': %d%n", request.uri(),
					Integer.valueOf(response.statusCode())));
		}

		final List<OwReport> reports = new ArrayList<>();
		try (InputStream is = response.body(); JsonReader reader = Json.createReader(is)) {
			JsonObject root = reader.readObject();

			JsonArray array = root.getJsonArray("list");
			if (array != null) {
				reports.addAll(
						array.stream().map(value -> parseForecastReport(OwReport.Type.HOURLY, (JsonObject) value))
								.collect(Collectors.toList()));
			}
		}

		return new OwForecast(location, reports);
	}

	/*
	 * Not available for the "Free" tier - minimum subscription level is "Startup"
	 */
	public OwForecast getDailyForecast(GeographicLocation location) throws IOException, InterruptedException {
		// https://api.openweathermap.org/data/2.5/forecast/daily?lat=51.1&lon=-0.73&appid=xxx
		HttpRequest request = HttpRequest.newBuilder()
				.uri(URI.create(String.format("%s/forecast/daily?lat=%f&lon=%f&units=metric&appid=%s", rootWeatherUri,
						Double.valueOf(location.getLatitude()), Double.valueOf(location.getLongitude()), apiKey)))
				.header("Content-Type", "application/json").build();

		HttpResponse<InputStream> response = httpClient.send(request, BodyHandlers.ofInputStream());
		if (response.statusCode() != 200) {
			System.out.format("Invalid response to request '%s': %d%n", request.uri(),
					Integer.valueOf(response.statusCode()));
			throw new IllegalStateException(String.format("Invalid response to request '%s': %d%n", request.uri(),
					Integer.valueOf(response.statusCode())));
		}

		final List<OwReport> reports = new ArrayList<>();
		try (InputStream is = response.body(); JsonReader reader = Json.createReader(is)) {
			JsonObject root = reader.readObject();

			JsonArray array = root.getJsonArray("list");
			if (array != null) {
				reports.addAll(array.stream().map(value -> parseForecastReport(OwReport.Type.DAILY, (JsonObject) value))
						.collect(Collectors.toList()));
			}
		}

		return new OwForecast(location, reports);
	}

	private static OwReport parseForecastReport(OwReport.Type type, JsonObject report) {
		// System.out.println(report);
		/*-
		 * {
		 * "coord": {"lon":-0.73,"lat":51.1},
		 * "weather": [
		 *   {"id":803,"main":"Clouds","description":"broken clouds","icon":"04d"}
		 * ],
		 * "base":"stations",
		 * "main": {"temp":282.52,"feels_like":279.9,"temp_min":281.48,"temp_max":284,"pressure":1028,"humidity":85},
		 * "visibility":10000,
		 * "wind": {"speed":5.14,"deg":250},
		 * "clouds":  {"all":75},
		 * "dt":1675347403,
		 * "sys": {"type":2,"id":2078889,"country":"GB","sunrise":1675323544,"sunset":1675356840},
		 * "timezone":0,
		 * "id":2646863,
		 * "name":"Hindhead",
		 * "cod":200
		 * }
		 */
		/*-
		 * {
		 *   "dt":1675803600,
		 *   "main": {
		 *     "temp":2.98,
		 *     "feels_like":2.98,
		 *     "temp_min":0.55,
		 *     "temp_max":2.98,
		 *     "pressure":1037,
		 *     "sea_level":1037,
		 *     "grnd_level":1007,
		 *     "humidity":78,
		 *     "temp_kf":2.43
		 *   }, "weather": [
		 *     {
		 *       "id":800,
		 *       "main":"Clear",
		 *       "description":"clear sky",
		 *       "icon":"01n"
		 *     }
		 *   ],
		 *   "clouds": {
		 *     "all":0
		 *   }, "wind": {
		 *     "speed":1.17,
		 *     "deg":120,
		 *     "gust":1.28
		 *   },
		 *   "visibility":10000,
		 *   "pop":0,
		 *   "sys": {
		 *     "pod":"n"
		 *   },
		 *   "dt_txt":"2023-02-07 21:00:00"
		 * }
		 */
		long epoch_time = report.getJsonNumber("dt").longValue() * 1000;
		JsonNumber jn = report.getJsonNumber("sunrise");
		long sunrise = jn == null ? 0 : jn.longValue() * 1000;
		jn = report.getJsonNumber("sunset");
		long sunset = jn == null ? 0 : jn.longValue() * 1000;
		jn = report.getJsonNumber("moonrise");
		long moonrise = jn == null ? 0 : jn.longValue() * 1000;
		jn = report.getJsonNumber("moonset");
		long moonset = jn == null ? 0 : jn.longValue() * 1000;
		jn = report.getJsonNumber("moon_phase");
		double moon_phase = jn == null ? 0 : jn.doubleValue();

		// Current weather report has TPH in a "main" object (plus has temp_min and
		// temp_max)
		JsonObject main = report.getJsonObject("main");
		if (main == null) {
			main = report;
		}
		// temp is an embedded object for daily, otherwise a double
		double temp;
		JsonValue jv = main.get("temp");
		if (jv.getValueType() == ValueType.NUMBER) {
			temp = ((JsonNumber) jv).doubleValue();
		} else {
			JsonObject obj = jv.asJsonObject();
			temp = obj.getJsonNumber("day").doubleValue();
		}
		double feels_like;
		jv = main.get("feels_like");
		if (jv.getValueType() == ValueType.NUMBER) {
			feels_like = ((JsonNumber) jv).doubleValue();
		} else {
			JsonObject obj = report.getJsonObject("feels_like");
			feels_like = obj.getJsonNumber("day").doubleValue();
		}
		// On the sea level if there is no sea_level or grnd_level data
		int pressure = main.getInt("pressure");
		jn = main.getJsonNumber("sea_level");
		Optional<Integer> sea_level;
		if (jn == null) {
			sea_level = Optional.empty();
		} else {
			sea_level = Optional.of(Integer.valueOf(jn.intValue()));
		}
		jn = main.getJsonNumber("grnd_level");
		Optional<Integer> grnd_level;
		if (jn == null) {
			grnd_level = Optional.empty();
		} else {
			grnd_level = Optional.of(Integer.valueOf(jn.intValue()));
		}
		int humidity = main.getInt("humidity");

		jn = report.getJsonNumber("dew_point");
		double dew_point = jn == null ? 0 : jn.doubleValue();

		// Current weather report has wind speed and direction in a "wind" object
		JsonObject wind = report.getJsonObject("wind");
		boolean has_wind = wind != null;
		if (wind == null) {
			wind = report;
		}
		double wind_speed = wind.getJsonNumber(has_wind ? "speed" : "wind_speed").doubleValue();
		int wind_direction = wind.getInt(has_wind ? "deg" : "wind_deg");
		jn = report.getJsonNumber(has_wind ? "gust" : "wind_gust");
		double wind_gust = jn == null ? 0 : jn.doubleValue();
		JsonArray weather_array = report.getJsonArray("weather");
		if (weather_array.size() > 1) {
			System.out.println("*** Got more than one weather report: got " + weather_array.size());
		}

		// Possible to get more than one, first is primary
		OwWeatherType weather = parseWeather((JsonObject) weather_array.get(0));
		int clouds;
		jv = report.get("clouds");
		if (jv.getValueType() == ValueType.OBJECT) {
			clouds = jv.asJsonObject().getInt("all");
		} else {
			clouds = ((JsonNumber) jv).intValue();
		}
		jn = report.getJsonNumber("visibility");
		int visibility = jn == null ? 0 : jn.intValue();
		jn = report.getJsonNumber("pop"); // Probability of precipitation
		int rain_prob = jn == null ? 0 : jn.intValue();
		double rain_in_last_period = 0;
		jv = report.get("rain");
		if (jv == null) {
			rain_in_last_period = 0;
		} else if (jv.getValueType() == ValueType.NUMBER) {
			rain_in_last_period = ((JsonNumber) jv).doubleValue();
		} else {
			JsonObject rain = jv.asJsonObject();
			if (rain.size() > 1) {
				System.out.println("*** Got more than one rain report - got " + rain.size());
			}
			jv = rain.values().stream().findFirst().orElse(null);
			if (jv != null) {
				rain_in_last_period = ((JsonNumber) jv).doubleValue();
			}
		}
		double snow_in_last_hour;
		jv = report.get("snow");
		if (jv == null) {
			snow_in_last_hour = 0;
		} else if (jv.getValueType() == ValueType.NUMBER) {
			snow_in_last_hour = ((JsonNumber) jv).doubleValue();
		} else {
			snow_in_last_hour = jv.asJsonObject().getJsonNumber("1h").doubleValue();
		}
		jn = report.getJsonNumber("uvi");
		double uvi = jn == null ? 0 : jn.doubleValue();

		return new OwReport(type, epoch_time, sunrise, sunset, moonrise, moonset, temp, feels_like, pressure, sea_level,
				grnd_level, humidity, dew_point, uvi, clouds, visibility, rain_prob, wind_speed, wind_gust,
				wind_direction, rain_in_last_period, snow_in_last_hour, weather, moon_phase);
	}

	private static OwWeatherType parseWeather(JsonObject weather) {
		return new OwWeatherType(weather.getInt("id"), weather.getString("main"), weather.getString("description"),
				weather.getString("icon"));
	}
}
