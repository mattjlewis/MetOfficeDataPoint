package com.diozero.weather.openweather;

import java.util.List;

import com.diozero.location.GeographicLocation;
import com.diozero.weather.Forecast;

public class OwForecast extends Forecast<OwReport> {
	public OwForecast(GeographicLocation location, List<OwReport> reports) {
		super(System.currentTimeMillis(), location, reports);
	}
}
