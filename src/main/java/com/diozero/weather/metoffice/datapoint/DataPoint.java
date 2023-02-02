package com.diozero.weather.metoffice.datapoint;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import com.diozero.location.GeographicLocation;

import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import jakarta.json.JsonString;
import jakarta.json.JsonValue;

/**
 * https://www.metoffice.gov.uk/binaries/content/assets/metofficegovuk/pdf/data/datapoint_api_reference.pdf
 */
public class DataPoint {
	private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-ddz"); // e.g.
																										// 2015-12-09Z

	private static final String ROOT_URL = "http://datapoint.metoffice.gov.uk/public/data";
	// Data categories
	private static final String VALUES = "val"; // Location-specific data
	private static final String TEXT = "text"; // Textual data
	private static final String IMAGE = "image"; // Stand-alone imagery
	private static final String LAYER = "layer"; // Map overlay imagery
	// Resource type: forecast or observation
	private static final String FORECAST = "wxfcs";
	private static final String OBSERVATIONS = "wxobs";
	// Data types
	private static final String JSON = "json";
	private static final String XML = "xml";
	// Requests
	private static final String SITELIST = "sitelist";
	private static final String CAPABILITIES = "capabilities";
	private static final String LATEST = "latest"; // For textual data only

	private String apiKey;
	private List<DataPointForecastLocation> locations;
	private HttpClient httpClient;
	private String rootUri;

	public DataPoint(String apiKey) {
		this.apiKey = apiKey;

		httpClient = HttpClient.newHttpClient();
		rootUri = ROOT_URL + "/" + VALUES + "/" + FORECAST + "/all/" + JSON;
	}

	public void loadSiteList() throws IOException {
		if (locations != null && !locations.isEmpty()) {
			return;
		}

		// Load the list of MetOffice weather monitoring locations
		System.out.println("Loading DataPoint site list...");

		HttpRequest request = HttpRequest.newBuilder().uri(URI.create(rootUri + "/" + SITELIST + "?key=" + apiKey))
				.header("Content-Type", "application/json").build();
		try {
			HttpResponse<InputStream> response = httpClient.send(request, BodyHandlers.ofInputStream());
			if (response.statusCode() != 200) {
				System.out.println("Invalid response to load site list " + response.statusCode());
				return;
			}

			try (InputStream is = response.body(); JsonReader reader = Json.createReader(is)) {
				locations = reader.readObject().getJsonObject("Locations").getJsonArray("Location").stream()
						.map(DataPoint::parseLocation).collect(Collectors.toList());
			}
			System.out.println("Loaded " + locations.size() + " locations");
		} catch (InterruptedException e) {
			// TODO Impl
		}
	}

	public void loadCapabilities(Resolution resolution) throws IOException {
		HttpRequest request = HttpRequest.newBuilder()
				.uri(URI.create(rootUri + "/" + CAPABILITIES + "?key=" + apiKey + "&res=" + resolution.getCode()))
				.header("Content-Type", "application/json").build();
		try {
			HttpResponse<InputStream> response = httpClient.send(request, BodyHandlers.ofInputStream());
			if (response.statusCode() != 200) {
				System.out.println("Invalid response to load capabilities " + response.statusCode());
				return;
			}

			try (InputStream is = response.body(); JsonReader reader = Json.createReader(is)) {
				Resource capabilities = parseResource(reader.readObject().getJsonObject("Resource"));
			}
		} catch (InterruptedException e) {
			// TODO Impl
		}
	}

	public DataPointForecast getForecast(GeographicLocation location, Resolution resolution) throws IOException {
		loadSiteList();

		// Get the closest MetOffice weather monitoring location
		DataPointForecastLocation fl = findClosest(location);
		System.out.println("Using forecast location that is closest to " + location + ": " + fl);

		return getForecast(fl, resolution);
	}

	public DataPointForecast getForecast(DataPointForecastLocation dpLocation, Resolution resolution)
			throws IOException {
		HttpRequest request = HttpRequest.newBuilder()
				.uri(URI.create(rootUri + "/" + dpLocation.getId() + "?key=" + apiKey + "&res=" + resolution.getCode()))
				.header("Content-Type", "application/json").build();
		try {
			HttpResponse<InputStream> response = httpClient.send(request, BodyHandlers.ofInputStream());
			if (response.statusCode() != 200) {
				System.out.println("Invalid response to load capabilities " + response.statusCode());
				return null;
			}

			ZonedDateTime forecast_date;
			DataPointForecastLocation forecast_location;
			List<DataPointReport> reports;
			Map<String, Param> params;
			String dv_type;
			try (InputStream is = response.body(); JsonReader reader = Json.createReader(is)) {
				JsonObject site_rep_json = reader.readObject().getJsonObject("SiteRep");

				params = parseParams(site_rep_json.getJsonObject("Wx").getJsonArray("Param"));

				JsonObject dv_obj = site_rep_json.getJsonObject("DV");
				forecast_date = DateTimeFormatter.ISO_ZONED_DATE_TIME.parse(dv_obj.getString("dataDate"),
						ZonedDateTime::from);
				// "Forecast" or "Obs"
				dv_type = dv_obj.getString("type");
				JsonObject location_obj = dv_obj.getJsonObject("Location");
				forecast_location = parseLocation(location_obj);
				reports = parsePeriods(location_obj.getJsonArray("Period"));
			}

			return new DataPointForecast(forecast_date.toInstant().toEpochMilli(), forecast_location, reports, params,
					dv_type);
		} catch (InterruptedException e) {
			// TODO Impl
		}

		return null;
	}

	public DataPointForecastLocation findClosest(GeographicLocation location) {
		double min_dist = Double.MAX_VALUE;
		DataPointForecastLocation closest = null;
		for (DataPointForecastLocation fl : locations) {
			double dist = location.distance(fl);
			if (dist < min_dist) {
				closest = fl;
				min_dist = dist;
			}
		}

		return closest;
	}

	public DataPointForecastLocation getLocation(int siteId) {
		for (DataPointForecastLocation location : locations) {
			if (location.getId() == siteId) {
				return location;
			}
		}

		return null;
	}

	private static Resource parseResource(JsonObject jsonObject) {
		Resource resource = new Resource();
		for (Entry<String, JsonValue> entry : jsonObject.entrySet()) {
			switch (entry.getKey()) {
			case "dataDate":
				resource.setDataDate(DateTimeFormatter.ISO_ZONED_DATE_TIME
						.parse(((JsonString) entry.getValue()).getString(), ZonedDateTime::from));
				break;
			case "res":
				resource.setRes(((JsonString) entry.getValue()).getString());
				break;
			case "type":
				resource.setType(((JsonString) entry.getValue()).getString());
				break;
			case "TimeSteps":
				JsonArray time_steps_json = ((JsonObject) entry.getValue()).getJsonArray("TS");

				List<ZonedDateTime> time_steps = new ArrayList<>();
				for (JsonValue value : time_steps_json) {
					JsonString s_value = (JsonString) value;
					time_steps
							.add(DateTimeFormatter.ISO_ZONED_DATE_TIME.parse(s_value.getString(), ZonedDateTime::from));
				}
				resource.setTimeSteps(time_steps);
				break;
			default:
				System.out.println("Unrecognised Resource key '" + entry.getKey() + "'");
			}
		}

		return resource;
	}

	private static Map<String, Param> parseParams(JsonArray param_json) {
		Map<String, Param> params = new HashMap<>();
		for (JsonValue value : param_json) {
			Param param = new Param();
			for (Entry<String, JsonValue> entry : ((JsonObject) value).entrySet()) {
				switch (entry.getKey()) {
				case "name":
					param.setName(((JsonString) entry.getValue()).getString());
					break;
				case "units":
					param.setUnits(((JsonString) entry.getValue()).getString());
					break;
				case "$":
					param.setDescription(((JsonString) entry.getValue()).getString());
					break;
				default:
					System.out.println("Unrecognised SiteRep key '" + entry.getKey() + "'");
				}
			}
			params.put(param.getName(), param);
		}

		return params;
	}

	private static DataPointForecastLocation parseLocation(JsonValue value) {
		return parseLocation((JsonObject) value);
	}

	private static DataPointForecastLocation parseLocation(JsonObject obj) {
		int id = 0;
		float elevation = 0;
		float latitude = 0;
		float longitude = 0;
		String name = "Unknown";
		String national_park = "";
		String region = "";
		String unitary_auth_area = "";
		String country = "";
		String continent = "";
		String obs_source = "";

		for (Entry<String, JsonValue> entry : obj.entrySet()) {
			switch (entry.getKey()) {
			case "id":
			case "i":
				id = Integer.parseInt(((JsonString) entry.getValue()).getString());
				break;
			case "elevation":
				elevation = Float.parseFloat(((JsonString) entry.getValue()).getString());
				break;
			case "latitude":
			case "lat":
				latitude = Float.parseFloat(((JsonString) entry.getValue()).getString());
				break;
			case "longitude":
			case "lon":
				longitude = Float.parseFloat(((JsonString) entry.getValue()).getString());
				break;
			case "name":
				name = ((JsonString) entry.getValue()).getString();
				break;
			case "nationalPark":
				national_park = ((JsonString) entry.getValue()).getString();
				break;
			case "region":
				region = ((JsonString) entry.getValue()).getString();
				break;
			case "unitaryAuthArea":
				unitary_auth_area = ((JsonString) entry.getValue()).getString();
				break;
			case "country":
				country = ((JsonString) entry.getValue()).getString();
				break;
			case "continent":
				continent = ((JsonString) entry.getValue()).getString();
				break;
			case "obsSource":
				obs_source = ((JsonString) entry.getValue()).getString();
				break;
			default:
				// System.out.println("Unrecognised Location key '" + entry.getKey() + "'");
				// Ignore
			}
		}

		return new DataPointForecastLocation(latitude, longitude, elevation, name, id, national_park, region,
				unitary_auth_area, country, continent, obs_source);
	}

	private static List<DataPointReport> parsePeriods(JsonArray periods_json) {
		List<DataPointReport> reports = new ArrayList<>();
		for (JsonValue value : periods_json) {
			JsonObject period_obj = (JsonObject) value;
			// Always equal to "Day"
			String period_type = period_obj.getString("type");
			ZonedDateTime period_start = DATE_FORMATTER.parse((period_obj.getString("value")), LocalDate::from)
					.atStartOfDay(ZoneId.systemDefault());
			for (JsonValue rep_json : period_obj.getJsonArray("Rep")) {
				reports.add(parseReport((JsonObject) rep_json, period_start));
			}
		}

		return reports;
	}

	private static DataPointReport parseReport(JsonObject object, ZonedDateTime periodStart) {
		int mins_after_midnight = 0;
		String wind_dir = null;
		int feels_like = 0;
		int wind_gust = 0;
		int rel_hum = 0;
		int pressure = 0;
		int precip_prob = 0;
		int wind_speed = 0;
		int temp = 0;
		Visibility visibility = null;
		DataPointWeatherType weather_type = null;
		int max_uvi = 0;
		for (Entry<String, JsonValue> entry : object.entrySet()) {
			switch (entry.getKey()) {
			case "$": // Number of minutes after midnight GMT on the day represented by the Period
				// object in which the Rep object is found
				mins_after_midnight = Integer.parseInt(((JsonString) entry.getValue()).getString());
				break;
			case "W": // Weather Type
				String weather_type_s = ((JsonString) entry.getValue()).getString().trim();
				if (!weather_type_s.equals(DataPointWeatherType.NA.getLabel())) {
					try {
						weather_type = DataPointWeatherType.values()[Integer.parseInt(weather_type_s)];
					} catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
						// Ignore
						System.out.println("Unknown weather type: " + weather_type_s);
					}
				}
				if (weather_type == null) {
					weather_type = DataPointWeatherType.NA;
				}
				break;
			case "V": // Visibility
				visibility = Visibility.valueOf(((JsonString) entry.getValue()).getString());
				break;
			case "T": // Temperature
				temp = Integer.parseInt(((JsonString) entry.getValue()).getString());
				break;
			case "F": // Feels Like Temperature
				feels_like = Integer.parseInt(((JsonString) entry.getValue()).getString());
				break;
			case "S": // Wind Speed
				wind_speed = Integer.parseInt(((JsonString) entry.getValue()).getString());
				break;
			case "G": // Wind Gust
				wind_gust = Integer.parseInt(((JsonString) entry.getValue()).getString());
				break;
			case "D": // Wind Direction
				wind_dir = ((JsonString) entry.getValue()).getString();
				break;
			case "P": // Mean sea level pressure in hectopascals (hPa)
				pressure = Integer.parseInt(((JsonString) entry.getValue()).getString());
				break;
			case "Pp": // Precipitation Probability
				precip_prob = Integer.parseInt(((JsonString) entry.getValue()).getString());
				break;
			case "H": // Screen Relative Humidity
				rel_hum = Integer.parseInt(((JsonString) entry.getValue()).getString());
				break;
			case "U": // Maximum Uv Index (0..11)
				max_uvi = Integer.parseInt(((JsonString) entry.getValue()).getString());
				break;
			default:
				System.out.println("Unrecognised Rep key '" + entry.getKey() + "'");
			}
		}

		return new DataPointReport(periodStart, mins_after_midnight, weather_type, visibility, temp, feels_like,
				pressure, wind_speed, wind_gust, wind_dir, precip_prob, rel_hum, max_uvi);
	}

	public enum Resolution {
		HOURLY("hourly"), // Only use for observations (not forecasts)
		THREE_HOURLY("3hourly"), DAILY("daily");

		private String code;

		Resolution(String code) {
			this.code = code;
		}

		public String getCode() {
			return code;
		}
	}
}
