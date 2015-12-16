package org.matt.metoffice.datapoint;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class WeatherCodes {
	static final Map<String, String> VALUES_MAP;
	static {
		Map<String, String> map = new HashMap<>();
		
		map.put("NA", "Not available");
		map.put("0", "Clear night");
		map.put("1", "Sunny day");
		map.put("2", "Partly cloudy (night)");
		map.put("3", "Partly cloudy (day)");
		map.put("4", "Not used");
		map.put("5", "Mist");
		map.put("6", "Fog");
		map.put("7", "Cloudy");
		map.put("8", "Overcast");
		map.put("9", "Light rain shower (night)");
		map.put("10", "Light rain shower (day)");
		map.put("11", "Drizzle");
		map.put("12", "Light rain");
		map.put("13", "Heavy rain shower (night)");
		map.put("14", "Heavy rain shower (day)");
		map.put("15", "Heavy rain");
		map.put("16", "Sleet shower (night)");
		map.put("17", "Sleet shower (day)");
		map.put("18", "Sleet");
		map.put("19", "Hail shower (night)");
		map.put("20", "Hail shower (day)");
		map.put("21", "Hail");
		map.put("22", "Light snow shower (night)");
		map.put("23", "Light snow shower (day)");
		map.put("24", "Light snow");
		map.put("25", "Heavy snow shower (night)");
		map.put("26", "Heavy snow shower (day)");
		map.put("27", "Heavy snow");
		map.put("28", "Thunder shower (night)");
		map.put("29", "Thunder shower (day)");
		map.put("30", "Thunder");
		
		VALUES_MAP = Collections.unmodifiableMap(map);
	}
}
