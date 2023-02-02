package com.diozero.weather.openweathermap;

import java.util.List;

import com.diozero.location.GeographicLocation;
import com.diozero.weather.Forecast;

public class OwmForecast extends Forecast<OwmReport> {
	public OwmForecast(GeographicLocation location, List<OwmReport> reports) {
		super(System.currentTimeMillis(), location, reports);
	}
}
