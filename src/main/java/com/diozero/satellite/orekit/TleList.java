package com.diozero.satellite.orekit;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;

import org.orekit.data.DataProvider;
import org.orekit.data.DataProvidersManager;
import org.orekit.errors.OrekitException;
import org.orekit.propagation.analytical.tle.TLE;

public class TleList {
	private Map<String, TLE> satelliteNameMapping;
	private Map<Integer, TLE> satelliteNumberMapping;
	
	public TleList() {
		satelliteNameMapping = new HashMap<>();
		satelliteNumberMapping = new HashMap<>();
	}
	
	public void load(String tleFilename) throws IOException, OrekitException {
		OrekitUtil.initialise();
		
		for (String path : System.getProperty(DataProvidersManager.OREKIT_DATA_PATH).split(File.pathSeparator)) {
			File p = new File(path);
			if (DataProvider.ZIP_ARCHIVE_PATTERN.matcher(path).matches()) {
				// TODO Load file from the ZIP/JAR file
			} else if (p.isDirectory()) {
				File tle_file = new File(p, tleFilename);
				if (tle_file.isFile()) {
					// Load the file from file system path
					try (BufferedReader br = new BufferedReader(new FileReader(tle_file))) {
						load(br);
						break;
					}
				}
			}
		}
	}
	
	public void load (URL url) throws IOException, OrekitException {
		URLConnection con = url.openConnection();
		try (BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream()))) {
			load(br);
		}
	}
	
	public void load(BufferedReader reader) throws IOException, OrekitException {
		int line_num = 0;
		String name = null;
		String line1 = null;
		String line2;
		while (true) {
			String line = reader.readLine();
			if (line == null) {
				break;
			}
			line = line.trim();
			if (line.length() == 0) {
				continue;
			}
			switch (line_num) {
			case 0:
				name = line;
				line_num++;
				break;
			case 1:
				if (line.startsWith("1 ")) {
					line1 = line;
					line_num++;
				} else {
					throw new IOException("Expecting line to start with '1 '");
				}
				break;
			case 2:
				if (line.startsWith("2 ")) {
					line2 = line;
					TLE tle = new TLE(line1, line2);
					satelliteNameMapping.put(name, tle);
					satelliteNumberMapping.put(Integer.valueOf(tle.getSatelliteNumber()), tle);
					line_num = 0;
				} else {
					throw new IOException("Expecting line to start with '2 '");
				}
				break;
			}
		}
	}

	public Map<String, TLE> getSatelliteNameMapping() {
		return satelliteNameMapping;
	}

	public Map<Integer, TLE> getSatelliteNumberMapping() {
		return satelliteNumberMapping;
	}
}
