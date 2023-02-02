package com.diozero.weather;

import java.util.List;

import com.diozero.location.GeographicLocation;

public class Forecast<E extends Report> {
	private long forecastTime;
	private GeographicLocation location;
	private List<E> reports;

	public Forecast(long forecastTime, GeographicLocation location, List<E> reports) {
		this.forecastTime = forecastTime;
		this.location = location;
		this.reports = reports;
	}

	public long getForecastTime() {
		return forecastTime;
	}

	public GeographicLocation getLocation() {
		return location;
	}

	public List<E> getReports() {
		return reports;
	}

	/**
	 * Get the weather report that is closest to the specified time.
	 *
	 * @param epochTime target report time (epoch time in milliseconds)
	 * @return the weather report that is closest to the specified time
	 */
	public E getClosestReport(long epochTime) {
		E closest_report = null;

		long min = Long.MAX_VALUE;
		for (E r : reports) {
			long delta = Math.abs(r.getEpochTime() - epochTime);
			if (delta < min) {
				closest_report = r;
				min = delta;
			}
		}

		return closest_report;
	}
}
