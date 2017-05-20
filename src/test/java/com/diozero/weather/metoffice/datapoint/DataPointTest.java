package org.matt.metoffice.datapoint;

import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;

import com.luckycatlabs.sunrisesunset.SunriseSunsetCalculator;

import gnu.getopt.Getopt;
import gnu.getopt.LongOpt;

public class DataPointTest {
	private static final String API_KEY = "2e020df4-1304-4e3a-b5e5-216f0647fc16";
	private static final int HASLEMERE_SITE_ID = 324247;

	public static void main(String[] args) {
		Float my_latitude = null;
		Float my_longitude = null;
		LongOpt[] long_opts = new LongOpt[2];
		long_opts[0] = new LongOpt("latitude", LongOpt.REQUIRED_ARGUMENT, null, 'a');
		long_opts[1] = new LongOpt("longitude", LongOpt.REQUIRED_ARGUMENT, null, 'o');
		Getopt g = new Getopt("DataPoint", args, ":a:o:", long_opts);
		int c;
		String arg;
		while ((c = g.getopt()) != -1) {
			switch (c) {
			case 0:
				arg = g.getOptarg();
				System.out.println("Got long option with value '" +
						/*(char)(new Integer(sb.toString())).intValue() +*/ "' with argument " +
                        ((arg != null) ? arg : "null"));
				break;
			case 1:
				System.out.println("I see you have return in order set and that a non-option argv element was just found " +
                             "with the value '" + g.getOptarg() + "'");
				break;
			case 2:
				arg = g.getOptarg();
				System.out.println("I know this, but pretend I didn't");
				//System.out.println("We picked option " + longopts[g.getLongind()].getName() +
				//		" with value " + ((arg != null) ? arg : "null"));
				break;
			case 'a':
				arg = g.getOptarg();
				if (arg == null || arg.isEmpty()) {
					System.out.println("Empty arg value for latitude");
				} else {
					my_latitude = Float.valueOf(arg);
				}
				break;
			case 'o':
				arg = g.getOptarg();
				if (arg == null || arg.isEmpty()) {
					System.out.println("Empty arg value for longitude");
				} else {
					my_longitude = Float.valueOf(arg);
				}
				break;
			case ':':
				System.out.println("Doh! You need an argument for option " + (char)g.getOptopt());
				break;
			case '?':
				System.out.println("The option '" + (char)g.getOptopt() + "' is not valid");
				break;
			default:
				System.out.println("getopt() returned " + c);
			}
		}
		System.out.format("my_lat=%f, my_lon=%f%n", my_latitude, my_longitude);
		
		DataPoint dp = new DataPoint(API_KEY);
		
		try {
			dp.init();
			
			Location location = null;
			if (my_latitude != null && my_longitude != null) {
				location = dp.findClosest(my_latitude.floatValue(), my_longitude.floatValue());
			}
			
			if (location == null) {
				location = dp.getLocation(HASLEMERE_SITE_ID);
			}
			System.out.println("Using location: " + location);
			
			com.luckycatlabs.sunrisesunset.dto.Location loc = new com.luckycatlabs.sunrisesunset.dto.Location(
					location.getLatitude(), location.getLongitude());
			SunriseSunsetCalculator calculator = new SunriseSunsetCalculator(loc, TimeZone.getDefault());
			Calendar now = Calendar.getInstance();
			Calendar sunrise = calculator.getOfficialSunriseCalendarForDate(now);
			Calendar sunset = calculator.getOfficialSunsetCalendarForDate(now);
			System.out.println("sunrise=" + sunrise.getTime() + ", sunset=" + sunset.getTime());
			
			Forecast f = dp.getForecast(location.getId(), DataPoint.Resolution.THREE_HOURLY);
			if (f != null) {
				Map<String, Param> params = f.getParams();
				Date start = f.getDataValue().getDataDate();
				String type = f.getDataValue().getType();
				System.out.println(type + " for " + start);
				for (Period period : f.getDataValue().getLocation().getPeriods()) {
					System.out.println(period.getType() + ": " + period.getValue());
					for (Report r : period.getReports()) {
						System.out.println("Time: " + r.getReportDateTime() + " [" + r.getMinutesAfterMidnight() + "]");
						Map<String, Object> values = r.getValues();
						for (Entry<String, Object> entry : values.entrySet()) {
							Param p = params.get(entry.getKey());
							System.out.println(p.getDescription() + ": " + entry.getValue() + " " + p.getUnits());
						}
						System.out.println();
					}
				}
			}
		} catch (IOException ioe) {
			System.out.println(ioe);
		}
	}
}
