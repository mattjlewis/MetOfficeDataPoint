package org.matt.metoffice.datapoint;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class RegionCodes {
	static final Map<String, String> VALUES_MAP;
	static {
		Map<String, String> map = new HashMap<>();
		
		map.put("os", "Orkney and Shetland");
		map.put("he", "Highland and Eilean Siar");
		map.put("gr", "Grampian");
		map.put("ta", "Tayside");
		map.put("st", "Strathclyde");
		map.put("dg", "Dumfries, Galloway, Lothian");
		map.put("ni", "Northern Ireland");
		map.put("yh", "Yorkshire and the Humber");
		map.put("ne", "Northeast England");
		map.put("em", "East Midlands");
		map.put("ee", "East of England");
		map.put("se", "London and Southeast England");
		map.put("nw", "Northwest England");
		map.put("wm", "West Midlands");
		map.put("sw", "Southwest England");
		map.put("wl", "Wales");
		map.put("uk", "United Kingdom");
		
		VALUES_MAP = Collections.unmodifiableMap(map);
	}
}
