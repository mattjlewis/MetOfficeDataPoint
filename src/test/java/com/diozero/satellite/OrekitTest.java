package com.diozero.satellite;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Calendar;
import java.util.Date;

import org.orekit.bodies.CelestialBody;
import org.orekit.bodies.CelestialBodyFactory;
import org.orekit.errors.OrekitException;
import org.orekit.propagation.analytical.tle.TLE;
import org.orekit.propagation.analytical.tle.TLESeries;
import org.orekit.time.AbsoluteDate;
import org.orekit.time.TimeScale;
import org.orekit.time.TimeScalesFactory;

import com.diozero.satellite.orekit.OrekitUtil;
import com.diozero.satellite.orekit.SatelliteUtil;
import com.diozero.satellite.orekit.TleList;

/**
 * ISS: 25544
 * TIANGONG 1: 37820
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
			
			try (BufferedReader br = new BufferedReader(new FileReader("src/main/resources/orekit-data/tle/iss.tle"))) {
				String name = br.readLine();
				String line1 = br.readLine();
				String line2 = br.readLine();
				TLE tle = new TLE(line1, line2);
				System.out.println(name + ": " + tle.getSatelliteNumber() + ", " + tle.getLaunchNumber() + ", " + tle.getLaunchYear() + ", " + tle.getDate());
				System.out.println(tle);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			TimeScale utc = TimeScalesFactory.getUTC();
			AbsoluteDate date = new AbsoluteDate(new Date(), utc);
			System.out.println("date: " + date);
			
			String series_name = "stations.txt";
			System.out.println();
			System.out.println("Series: " + series_name);
			TLESeries series = new TLESeries(series_name, true);
			System.out.println("Available satellite numbers: " + series.getAvailableSatelliteNumbers());
			TLE tle = series.getClosestTLE(date);
			System.out.println("Closest TLE: " + tle.getSatelliteNumber() + ", " + tle.getLaunchNumber() + ", " + tle.getLaunchYear() + ", " + tle.getDate());
			System.out.println(tle);
			
			// ISS TLEs
			System.out.println();
			series_name = "iss.txt";
			series = new TLESeries(null, true);
			System.out.println("Series: " + series_name);
			try {
				URL url = new URL("http://celestrak.com/NORAD/elements/supplemental/" + series_name);
				URLConnection con = url.openConnection();
				series.loadData(con.getInputStream(), "ISS TLEs");
				System.out.println("Available satellite numbers: " + series.getAvailableSatelliteNumbers());
				tle = series.getClosestTLE(date);
				System.out.println("Closest TLE: " + tle.getSatelliteNumber() + ", " + tle.getLaunchNumber() + ", " + tle.getLaunchYear() + ", " + tle.getDate());
				System.out.println(tle);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			// Visual satellites
			System.out.println();
			series_name = "visual.txt";
			series = new TLESeries(null, true);
			System.out.println("Series: " + series_name);
			try {
				URL url = new URL("http://celestrak.com/NORAD/elements/" + series_name);
				URLConnection con = url.openConnection();
				series.loadData(con.getInputStream(), "NORAD Visual Satellites");
				System.out.println("Available satellite numbers: " + series.getAvailableSatelliteNumbers());
				tle = series.getClosestTLE(date);
				System.out.println("Closest TLE: " + tle.getSatelliteNumber() + ", " + tle.getLaunchNumber() + ", " + tle.getLaunchYear() + ", " + tle.getDate());
				System.out.println(tle);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			TleList list = new TleList();
			try {
				list.load("tle/" + series_name);
				System.out.println(list.getSatelliteNameMapping());
				System.out.println(list.getSatelliteNumberMapping());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			ObservationLocation obs_loc = new ObservationLocation(51.0900521, -0.7132183, 150, "Haslemere");
			
			AbsoluteDate start_date = new AbsoluteDate(new Date(), utc);
			Calendar cal = Calendar.getInstance();
			cal.add(Calendar.DAY_OF_MONTH, 8);
			AbsoluteDate end_date = new AbsoluteDate(cal.getTime(), utc);
			tle = series.getFirst();
			
			System.out.println();
			System.out.println(SatelliteUtil.calculateVisibleFlybys(tle, obs_loc, 10, 25, start_date, end_date));
		} catch (OrekitException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
