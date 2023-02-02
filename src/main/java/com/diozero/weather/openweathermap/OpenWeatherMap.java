package com.diozero.weather.openweathermap;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.diozero.location.GeographicLocation;

import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonNumber;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import jakarta.json.JsonValue;
import jakarta.json.JsonValue.ValueType;

public class OpenWeatherMap {
	private String apiKey;
	private HttpClient httpClient;
	private String rootWeatherUri;

	public OpenWeatherMap(String apiKey) {
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
			System.out.println("Invalid response to load capabilities " + response.statusCode());
			return null;
		}

		try (InputStream is = response.body(); JsonReader reader = Json.createReader(is)) {
			return reader.readArray().stream().map(jv -> jv.asJsonObject()).map(OpenWeatherMap::parseLocation)
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

	public OwmReport getCurrentWeather(GeographicLocation location) throws IOException {
		// https://api.openweathermap.org/data/2.5/weather?lat=51.1&lon=-0.73&appid=xxx
		HttpRequest request = HttpRequest.newBuilder()
				.uri(URI.create(rootWeatherUri + "/weather?lat=" + location.getLatitude() + "&lon="
						+ location.getLongitude() + "&units=metric" + "&appid=" + apiKey))
				.header("Content-Type", "application/json").build();
		try {
			HttpResponse<InputStream> response = httpClient.send(request, BodyHandlers.ofInputStream());
			if (response.statusCode() != 200) {
				System.out.println("Invalid response to load capabilities " + response.statusCode());
				return null;
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
				return parseForecastReport(OwmReport.Type.CURRENT, reader.readObject());
			}
		} catch (InterruptedException e) {
			// TODO Impl
		}

		return null;
	}

	public OwmForecast getForecast(GeographicLocation location) throws IOException {
		// https://api.openweathermap.org/data/2.5/onecall?lat=51.1&lon=-0.73
		// &exclude=current,minutely,hourly,daily,alerts
		// &appid=xxx
		HttpRequest request = HttpRequest.newBuilder()
				.uri(URI.create(rootWeatherUri + "/onecall?lat=" + location.getLatitude() + "&lon="
						+ location.getLongitude() + "&exclude=minutely" + "&units=metric" + "&appid=" + apiKey))
				.header("Content-Type", "application/json").build();
		try {
			HttpResponse<InputStream> response = httpClient.send(request, BodyHandlers.ofInputStream());
			if (response.statusCode() != 200) {
				System.out.println("Invalid response to load capabilities " + response.statusCode());
				return null;
			}

			final List<OwmReport> reports = new ArrayList<>();
			try (InputStream is = response.body(); JsonReader reader = Json.createReader(is)) {
				JsonObject root = reader.readObject();

				JsonObject obj = root.getJsonObject("current");
				if (obj != null) {
					reports.add(parseForecastReport(OwmReport.Type.CURRENT, obj));
				}

				JsonArray array = root.getJsonArray("minutely");
				if (array != null) {
					reports.addAll(array.stream()
							.map(value -> parseForecastReport(OwmReport.Type.MINUTELY, (JsonObject) value))
							.collect(Collectors.toList()));
				}

				array = root.getJsonArray("hourly");
				if (array != null) {
					reports.addAll(
							array.stream().map(value -> parseForecastReport(OwmReport.Type.HOURLY, (JsonObject) value))
									.collect(Collectors.toList()));
				}

				array = root.getJsonArray("daily");
				if (array != null) {
					reports.addAll(
							array.stream().map(value -> parseForecastReport(OwmReport.Type.DAILY, (JsonObject) value))
									.collect(Collectors.toList()));
				}
			}

			return new OwmForecast(location, reports);
		} catch (InterruptedException e) {
			// TODO Impl
		}

		return null;
	}

	private static OwmReport parseForecastReport(OwmReport.Type type, JsonObject forecast) {
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
		long epoch_time = forecast.getJsonNumber("dt").longValue() * 1000;
		JsonNumber jn = forecast.getJsonNumber("sunrise");
		long sunrise = jn == null ? 0 : jn.longValue() * 1000;
		jn = forecast.getJsonNumber("sunset");
		long sunset = jn == null ? 0 : jn.longValue() * 1000;
		jn = forecast.getJsonNumber("moonrise");
		long moonrise = jn == null ? 0 : jn.longValue() * 1000;
		jn = forecast.getJsonNumber("moonset");
		long moonset = jn == null ? 0 : jn.longValue() * 1000;
		jn = forecast.getJsonNumber("moon_phase");
		double moon_phase = jn == null ? 0 : jn.doubleValue();

		// Current weather forecast has TPH in a "main" object (plus has temp_min and
		// temp_max)
		JsonObject main = forecast.getJsonObject("main");
		if (main == null) {
			main = forecast;
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
			JsonObject obj = forecast.getJsonObject("feels_like");
			feels_like = obj.getJsonNumber("day").doubleValue();
		}
		// On the sea level if there is no sea_level or grnd_level data
		int pressure = main.getInt("pressure");
		jn = main.getJsonNumber("sea_level");
		jn = main.getJsonNumber("grnd_level");
		int humidity = main.getInt("humidity");

		jn = forecast.getJsonNumber("dew_point");
		double dew_point = jn == null ? 0 : jn.doubleValue();

		// Current weather foreast has wind speed and direction in a "wind" object
		JsonObject wind = forecast.getJsonObject("wind");
		boolean has_wind = wind != null;
		if (wind == null) {
			wind = forecast;
		}
		double wind_speed = wind.getJsonNumber(has_wind ? "speed" : "wind_speed").doubleValue();
		int wind_direction = wind.getInt(has_wind ? "deg" : "wind_deg");
		jn = forecast.getJsonNumber("wind_gust");
		double wind_gust = jn == null ? 0 : jn.doubleValue();
		JsonArray weather_array = forecast.getJsonArray("weather");
		if (weather_array.size() > 1) {
			System.out.println("Got more than one weather report: got " + weather_array.size());
		}

		// Possible to get more than one, first is primary
		OwmWeatherType weather = parseWeather((JsonObject) weather_array.get(0));
		int clouds;
		jv = forecast.get("clouds");
		if (jv.getValueType() == ValueType.OBJECT) {
			clouds = jv.asJsonObject().getInt("all");
		} else {
			clouds = ((JsonNumber) jv).intValue();
		}
		jn = forecast.getJsonNumber("visibility");
		int visibility = jn == null ? 0 : jn.intValue();
		jn = forecast.getJsonNumber("pop"); // Probability of precipitation
		int rain_prob = jn == null ? 0 : jn.intValue();
		double rain_in_last_hour;
		jv = forecast.get("rain");
		if (jv == null) {
			rain_in_last_hour = 0;
		} else if (jv.getValueType() == ValueType.NUMBER) {
			rain_in_last_hour = ((JsonNumber) jv).doubleValue();
		} else {
			rain_in_last_hour = jv.asJsonObject().getJsonNumber("1h").doubleValue();
		}
		double snow_in_last_hour;
		jv = forecast.get("snow");
		if (jv == null) {
			snow_in_last_hour = 0;
		} else if (jv.getValueType() == ValueType.NUMBER) {
			snow_in_last_hour = ((JsonNumber) jv).doubleValue();
		} else {
			snow_in_last_hour = jv.asJsonObject().getJsonNumber("1h").doubleValue();
		}
		jn = forecast.getJsonNumber("uvi");
		double uvi = jn == null ? 0 : jn.doubleValue();

		return new OwmReport(type, epoch_time, sunrise, sunset, moonrise, moonset, temp, feels_like, pressure, humidity,
				dew_point, uvi, clouds, visibility, rain_prob, wind_speed, wind_gust, wind_direction, rain_in_last_hour,
				snow_in_last_hour, weather, moon_phase);
	}

	private static OwmWeatherType parseWeather(JsonObject weather) {
		return new OwmWeatherType(weather.getInt("id"), weather.getString("main"), weather.getString("description"),
				weather.getString("icon"));
	}
}
