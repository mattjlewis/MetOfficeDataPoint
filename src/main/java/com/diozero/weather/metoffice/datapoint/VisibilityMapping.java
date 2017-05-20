package org.matt.metoffice.datapoint;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class VisibilityCodes {
	static final Map<String, String> VALUES_MAP;
	static {
		Map<String, String> map = new HashMap<>();
		
		map.put("EX", "Excellent - More than 40 km");
		map.put("VG", "Very Good - Between 20-40 km");
		map.put("GO", "Good - Between 10-20 km");
		map.put("MO", "Moderate - Between 4-10 km");
		map.put("PO", "Poor - Between 1-4 km");
		map.put("PV", "Very Poor - Less than 1 km");
		map.put("UN", "Unknown");
		
		VALUES_MAP = Collections.unmodifiableMap(map);
	}
}
