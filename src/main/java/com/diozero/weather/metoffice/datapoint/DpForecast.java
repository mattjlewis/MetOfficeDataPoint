package com.diozero.weather.metoffice.datapoint;

import java.util.List;
import java.util.Map;

import com.diozero.weather.Forecast;

public class DpForecast extends Forecast<DpReport> {
	private DpForecastLocation location;
	private Map<String, Param> params;
	private String type;

	public DpForecast(long forecastTime, DpForecastLocation location, List<DpReport> reports,
			Map<String, Param> params, String type) {
		super(forecastTime, location, reports);

		this.location = location;
		this.params = params;
		this.type = type;
	}

	public Map<String, Param> getParams() {
		return params;
	}

	public String getType() {
		return type;
	}

	@Override
	public DpForecastLocation getLocation() {
		return location;
	}
}
