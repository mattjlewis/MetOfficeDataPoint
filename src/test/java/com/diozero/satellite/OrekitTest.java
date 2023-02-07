package com.diozero.satellite;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Calendar;
import java.util.Date;

import org.orekit.bodies.CelestialBody;
import org.orekit.bodies.CelestialBodyFactory;
import org.orekit.errors.OrekitException;
import org.orekit.propagation.analytical.tle.TLE;
import org.orekit.time.AbsoluteDate;
import org.orekit.time.TimeScale;
import org.orekit.time.TimeScalesFactory;

import com.diozero.location.GeographicLocation;
import com.diozero.satellite.orekit.OrekitUtil;
import com.diozero.satellite.orekit.SatelliteUtil;
import com.diozero.satellite.orekit.TleList;
import com.diozero.satellite.orekit.TleSeries;

/**
 * ISS: 25544 TIANGONG 1: 37820
 *
 * @author MATTLEWI
 *
 */
public class OrekitTest {
	public static void main(String[] args) {
		try {
			OrekitUtil.initialise();

			CelestialBody moon = CelestialBodyFactory.getMoon();
			System.out.println("Loaded body " + moon.getName());
			CelestialBody earth = CelestialBodyFactory.getEarth();
			System.out.println("Loaded body " + earth.getName());

			// From https://live.ariss.org/iss.txt, also
			// https://celestrak.com/NORAD/elements/supplemental/iss.txt
			try (BufferedReader br = new BufferedReader(new FileReader("src/main/resources/orekit-data/tle/iss.txt"))) {
				String name = br.readLine();
				String line1 = br.readLine();
				String line2 = br.readLine();
				TLE tle = new TLE(line1, line2);
				System.out.println(name + ": " + tle.getSatelliteNumber() + ", " + tle.getLaunchNumber() + ", "
						+ tle.getLaunchYear() + ", " + tle.getDate());
				System.out.println(tle);
			} catch (IOException e) {
				e.printStackTrace();
			}

			TimeScale utc = TimeScalesFactory.getUTC();
			AbsoluteDate date = new AbsoluteDate(new Date(), utc);
			System.out.println("date: " + date);

			// Load only stations.txt from http://celestrak.org/NORAD/elements/stations.txt
			// "src/main/resources/orekit-data/tle/stations.txt";
			String series_name = "stations.txt";
			System.out.println();
			System.out.println("Series: " + series_name);
			TleSeries series = new TleSeries(series_name, true);
			System.out.println("Available satellite numbers [" + +series.getAvailableSatelliteNumbers().size() + "] : "
					+ series.getAvailableSatelliteNumbers());
			TLE tle = series.getClosestTLE(date);
			System.out.println("Closest TLE: " + tle.getSatelliteNumber() + ", " + tle.getLaunchNumber() + ", "
					+ tle.getLaunchYear() + ", " + tle.getDate());

			// Load iss.txt from the Internet (ISS TLEs)
			System.out.println();
			series_name = "iss.txt";
			series = new TleSeries(null, true);
			System.out.println("Series: " + series_name);
			try {
				URL url = new URL("https://celestrak.com/NORAD/elements/supplemental/" + series_name);
				URLConnection con = url.openConnection();
				try (InputStream is = con.getInputStream()) {
					series.loadData(is, "ISS TLEs");
				}
				System.out.println("Available satellite numbers: " + series.getAvailableSatelliteNumbers());
				tle = series.getClosestTLE(date);
				System.out.println("Closest TLE: " + tle.getSatelliteNumber() + ", " + tle.getLaunchNumber() + ", "
						+ tle.getLaunchYear() + ", " + tle.getDate());
				System.out.println(tle);
			} catch (IOException e) {
				e.printStackTrace();
			}

			// Visual satellites
			System.out.println();
			series_name = "visual.txt";
			series = new TleSeries(null, true);
			System.out.println("Series: " + series_name);
			try {
				URL url = new URL("https://celestrak.com/NORAD/elements/" + series_name);
				URLConnection con = url.openConnection();
				try (InputStream is = con.getInputStream()) {
					series.loadData(is, "NORAD Visual Satellites");
				}
				System.out.println("Available satellite numbers: " + series.getAvailableSatelliteNumbers());
				tle = series.getClosestTLE(date);
				System.out.println("Closest TLE: " + tle.getSatelliteNumber() + ", " + tle.getLaunchNumber() + ", "
						+ tle.getLaunchYear() + ", " + tle.getDate());
				System.out.println(tle);
			} catch (IOException e) {
				e.printStackTrace();
			}
			System.out.println();

			series_name = "visual.txt";
			TleList tle_list = new TleList();
			try {
				tle_list.load("tle/" + series_name);
				System.out.println("Series: " + series_name);
				System.out.println(tle_list.getSatelliteNameMapping());
				System.out.println(tle_list.getSatelliteNumberMapping());
			} catch (IOException e) {
				e.printStackTrace();
			}
			System.out.println();

			GeographicLocation obs_loc = new GeographicLocation(51.0900521, -0.7132183, 150, "Haslemere", "GB");

			int days = 5;
			AbsoluteDate start_date = new AbsoluteDate(new Date(), utc);
			Calendar cal = Calendar.getInstance();
			cal.add(Calendar.DAY_OF_MONTH, days);
			AbsoluteDate end_date = new AbsoluteDate(cal.getTime(), utc);

			String tle_name = "ISS (ZARYA)";
			tle = tle_list.getSatelliteNameMapping().get(tle_name);

			System.out.println("Flybys for TLE " + tle_name + " over the next " + days + " days:");
			SatelliteUtil.calculateVisibleFlybys(tle, obs_loc, 10, 45, start_date, end_date).forEach(OrekitTest::print);
		} catch (OrekitException e) {
			e.printStackTrace();
		}
	}

	private static void print(SatelliteFlyby flyby) {
		System.out.println(flyby);
	}
}
