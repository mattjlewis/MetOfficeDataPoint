package org.matt.metoffice.datapoint;

import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import javax.json.*;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.spatial4j.core.context.SpatialContext;
import com.spatial4j.core.shape.Point;

public class DataPoint {
	private static final String DATE_TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss'Z'";	// e.g. 2015-12-09T15:00:00Z
	private static final String DATE_FORMAT = "yyyy-MM-dd'Z'";	// e.g. 2015-12-09Z
	
	private static final String ROOT_URL = "http://datapoint.metoffice.gov.uk/public/data";
	// Data categories
	private static final String VALUES = "val";		// Location-specific data
	private static final String TEXT = "text";		// Textual data
	private static final String IMAGE = "image";	// Stand-alone imagery
	private static final String LAYER = "layer";	// Map overlay imagery
	// Resource type: forecast or observation
	private static final String FORECAST = "wxfcs";
	private static final String OBSERVATIONS = "wxobs";
	// Data types
	private static final String JSON = "json";
	private static final String XML = "xml";
	// Requests
	private static final String SITELIST = "sitelist";
	private static final String CAPABILITIES = "capabilities";
	private static final String LATEST = "latest";	// For textual data only

	private String apiKey;
	private WebTarget rootTarget;
	private List<Location> locations;
	private Resource capabilities;
	private SpatialContext spatialContext;
	
	public DataPoint(String apiKey) {
		this.apiKey = apiKey;
		Client client = ClientBuilder.newClient();
		rootTarget = client.target(ROOT_URL + "/" + VALUES + "/" + FORECAST + "/all/" + JSON);
		
		spatialContext = SpatialContext.GEO;
	}
	
	public void loadSiteList() throws IOException {
		WebTarget target = rootTarget.path(SITELIST).queryParam("key", apiKey);
		Response response = target.request(MediaType.APPLICATION_JSON_TYPE).get();
		
		if (response.getStatus() != Response.Status.OK.getStatusCode()) {
			System.out.println("Invalid response to load site list " + response.getStatusInfo());
			return;
		}
		
		try (InputStream locations_is = response.readEntity(InputStream.class)) {
			try (JsonReader reader = Json.createReader(locations_is)) {
				locations = reader.readObject().getJsonObject("Locations").getJsonArray("Location").stream().map(this::parseLocation).collect(Collectors.toList());
				/*
				locations = new ArrayList<>();
				for (JsonValue value : reader.readObject().getJsonObject("Locations").getJsonArray("Location")) {
					Location location = parseLocation((JsonObject)value);
					locations.add(location);
				}
				*/
			}
		}
	}

	public void init() throws IOException {
		loadCapabilities(DataPoint.Resolution.THREE_HOURLY);
		loadSiteList();
	}
	
	public void loadCapabilities(Resolution resolution) throws IOException {
		WebTarget target = rootTarget.path(CAPABILITIES).queryParam("key", apiKey).queryParam("res", resolution.getCode());
		Response response = target.request(MediaType.APPLICATION_JSON_TYPE).get();
		
		if (response.getStatus() != Response.Status.OK.getStatusCode()) {
			System.out.println("Invalid response to load capabilities " + response.getStatusInfo());
			return;
		}
		
		try (InputStream is = response.readEntity(InputStream.class)) {
			try (JsonReader reader = Json.createReader(is)) {
				capabilities = parseResource(reader.readObject().getJsonObject("Resource"));
			}
		}
	}

	public Forecast getForecast(int siteId, Resolution resolution) throws IOException {
		WebTarget target = rootTarget.path(Integer.toString(siteId)).queryParam("key", apiKey).queryParam("res", resolution.getCode());
		Response response = target.request(MediaType.APPLICATION_JSON_TYPE).get();
		
		if (response.getStatus() != Response.Status.OK.getStatusCode()) {
			System.out.println("Invalid response to get forecast " + response.getStatusInfo());
			return null;
		}
		
		Forecast forecast = new Forecast();
		try (InputStream is = response.readEntity(InputStream.class)) {
			try (JsonReader reader = Json.createReader(is)) {
				JsonObject site_rep_json = reader.readObject().getJsonObject("SiteRep");
				
				JsonObject wx_json = site_rep_json.getJsonObject("Wx");

				forecast.setParams(parseParams(wx_json.getJsonArray("Param")));
				forecast.setDataValue(parseDataValue(site_rep_json.getJsonObject("DV")));
			}
		}
		
		return forecast;
	}
	
	public Location findClosest(float latitude, float longitude) {
		Point p = spatialContext.makePoint(latitude, longitude);
		
		// Can this be done with streams?
		//MyPoint my_p = new MyPoint(p);
		//OptionalDouble min = locations.stream().mapToDouble(my_p::calcDistance).min();
		
		double min_dist = Double.MAX_VALUE;
		Location closest = null;
		for (Location location : locations) {
			double dist = spatialContext.calcDistance(p, location.getPoint());
			if (dist < min_dist) {
				closest = location;
				min_dist = dist;
			}
		}
		
		return closest;
	}

	public Location getLocation(int siteId) {
		for (Location location : locations) {
			if (location.getId() == siteId) {
				return location;
			}
		}
		
		return null;
	}

	private static Resource parseResource(JsonObject jsonObject) {
		Resource resource = new Resource();
		DateFormat date_format = new SimpleDateFormat(DATE_TIME_FORMAT);
		for (Entry<String, JsonValue> entry : jsonObject.entrySet()) {
			switch (entry.getKey()) {
			case "dataDate":
				try {
					resource.setDataDate(date_format.parse(((JsonString)entry.getValue()).getString()));
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				break;
			case "res":
				resource.setRes(((JsonString)entry.getValue()).getString());
				break;
			case "type":
				resource.setType(((JsonString)entry.getValue()).getString());
				break;
			case "TimeSteps":
				JsonArray time_steps_json = ((JsonObject)entry.getValue()).getJsonArray("TS");
				
				List<Date> time_steps = new ArrayList<>();
				for (JsonValue value : time_steps_json) {
					JsonString s_value = (JsonString)value;
					try {
						time_steps.add(date_format.parse(s_value.getString()));
					} catch (ParseException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				resource.setTimeSteps(time_steps);
				break;
			default:
				System.out.println("Unrecognised Resource key '" + entry.getKey() + "'");
			}
		}
		
		return resource;
	}

	private DataValue parseDataValue(JsonObject dv_json) {
		DateFormat date_format = new SimpleDateFormat(DATE_TIME_FORMAT);
		
		DataValue data_value = new DataValue();
		for (Entry<String, JsonValue> entry : dv_json.entrySet()) {
			switch (entry.getKey()) {
			case "dataDate":
				try {
					data_value.setDataDate(date_format.parse(((JsonString)entry.getValue()).getString()));
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				break;
			case "type":
				data_value.setType(((JsonString)entry.getValue()).getString());
				break;
			case "Location":
				data_value.setLocation(parseLocation((JsonObject)entry.getValue()));
				break;
			default:
				System.out.println("Unrecognised DV key '" + entry.getKey() + "'");
			}
		}
		
		return data_value;
	}

	private static Map<String, Param> parseParams(JsonArray param_json) {
		Map<String, Param> params = new HashMap<>();
		for (JsonValue value : param_json) {
			Param param = new Param();
			for (Entry<String, JsonValue> entry : ((JsonObject)value).entrySet()) {
				switch (entry.getKey()) {
				case "name":
					param.setName(((JsonString)entry.getValue()).getString());
					break;
				case "units":
					param.setUnits(((JsonString)entry.getValue()).getString());
					break;
				case "$":
					param.setDescription(((JsonString)entry.getValue()).getString());
					break;
				default:
					System.out.println("Unrecognised SiteRep key '" + entry.getKey() + "'");
				}
			}
			params.put(param.getName(), param);
		}
		
		return params;
	}
	
	private Location parseLocation(JsonValue value) {
		return parseLocation((JsonObject)value);
	}

	private Location parseLocation(JsonObject obj) {
		Location location = new Location();
		Float lat = null;
		Float lon = null;
		for (Entry<String, JsonValue> entry : obj.entrySet()) {
			switch (entry.getKey()) {
			case "id":
			case "i":
				location.setId(Integer.parseInt(((JsonString)entry.getValue()).getString()));
				break;
			case "elevation":
				location.setElevation(Float.valueOf(((JsonString)entry.getValue()).getString()));
				break;
			case "latitude":
			case "lat":
				lat = Float.valueOf(Float.parseFloat(((JsonString)entry.getValue()).getString()));
				location.setLatitude(lat.floatValue());
				break;
			case "longitude":
			case "lon":
				lon = Float.valueOf(Float.parseFloat(((JsonString)entry.getValue()).getString()));
				location.setLongitude(lon.floatValue());
				break;
			case "name":
				location.setName(((JsonString)entry.getValue()).getString());
				break;
			case "nationalPark":
				location.setNationalPark(((JsonString)entry.getValue()).getString());
				break;
			case "region":
				location.setRegion(((JsonString)entry.getValue()).getString());
				break;
			case "unitaryAuthArea":
				location.setUnitaryAuthArea(((JsonString)entry.getValue()).getString());
				break;
			case "country":
				location.setCountry(((JsonString)entry.getValue()).getString());
				break;
			case "continent":
				location.setContinent(((JsonString)entry.getValue()).getString());
				break;
			case "Period":
				location.setPeriods(parsePeriods((JsonArray)entry.getValue()));
				break;
			default:
				System.out.println("Unrecognised Location key '" + entry.getKey() + "'");
			}
		}
		if (lat != null && lon != null) {
			location.setPoint(spatialContext.makePoint(lat.floatValue(), lon.floatValue()));
		}
		
		return location;
	}

	private static List<Period> parsePeriods(JsonArray periods_json) {
		List<Period> periods = new ArrayList<>();
		DateFormat date_format = new SimpleDateFormat(DATE_FORMAT);
		for (JsonValue value : periods_json) {
			Period period = new Period();
			for (Entry<String, JsonValue> entry : ((JsonObject)value).entrySet()) {
				switch (entry.getKey()) {
				case "type":
					period.setType(((JsonString)entry.getValue()).getString());
					break;
				case "value":
					try {
						period.setValue(date_format.parse(((JsonString)entry.getValue()).getString()));
					} catch (ParseException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					break;
				case "Rep":
					List<Report> reports = new ArrayList<>();
					for (JsonValue rep_json : ((JsonArray)entry.getValue())) {
						Report report = parseReport((JsonObject)rep_json, period.getValue());
						reports.add(report);
					}
					period.setReports(reports);
					break;
				default:
					System.out.println("Unrecognised Period key '" + entry.getKey() + "'");
				}
			}
			periods.add(period);
		}
		
		return periods;
	}

	private static Report parseReport(JsonObject object, Date periodStart) {
		Report report = new Report(periodStart);
		for (Entry<String, JsonValue> entry : object.entrySet()) {
			switch (entry.getKey()) {
			case "D":	// Wind Direction
				report.setWindDirection(((JsonString)entry.getValue()).getString());
				break;
			case "F":	// Feels Like Temperature
				report.setFeelsLikeTemperature(Integer.parseInt(((JsonString)entry.getValue()).getString()));
				break;
			case "G":	// Wind Gust
				report.setWindGust(Integer.parseInt(((JsonString)entry.getValue()).getString()));
				break;
			case "H":	// Screen Relative Humidity
				report.setScreenRelativeHumidity(Integer.parseInt(((JsonString)entry.getValue()).getString()));
				break;
			case "Pp":	// Precipitation Probability
				report.setPrecipitationProbability(Integer.parseInt(((JsonString)entry.getValue()).getString()));
				break;
			case "S":	// Wind Speed
				report.setWindSpeed(Integer.parseInt(((JsonString)entry.getValue()).getString()));
				break;
			case "T":	// Temperature
				report.setTemperature(Integer.parseInt(((JsonString)entry.getValue()).getString()));
				break;
			case "V":	// Visibility
				report.setVisibility(((JsonString)entry.getValue()).getString());
				break;
			case "W":	// Weather Type
				report.setWeatherType(((JsonString)entry.getValue()).getString());
				break;
			case "U":	// Maximum Uv Index
				report.setMaximumUvIndex(Integer.parseInt(((JsonString)entry.getValue()).getString()));
				break;
			case "$":	// Number of minutes after midnight GMT on the day represented by the Period object in which the Rep object is found
				report.setMinutesAfterMidnight(Integer.parseInt(((JsonString)entry.getValue()).getString()));
				break;
			default:
				System.out.println("Unrecognised Rep key '" + entry.getKey() + "'");
			}
		}
		
		return report;
	}
	
	public enum Resolution {
		HOURLY("hourly"),			// Only use for observations (not forecasts)
		THREE_HOURLY("3hourly"),
		DAILY("daily");
		
		private String code;
		
		private Resolution(String code) {
			this.code = code;
		}
		
		public String getCode() {
			return code;
		}
	}
}

class MyPoint {
	private Point point;
	private SpatialContext spatialContext;
	
	public MyPoint(Point point) {
		this.point = point;
		spatialContext = SpatialContext.GEO;
	}
	
	public Point getPoint() {
		return point;
	}

	public double calcDistance(Location location) {
		return spatialContext.calcDistance(point, location.getPoint());
	}
}
