package com.diozero.weather.metoffice.datapoint;

import java.util.List;
import java.util.Map;

import com.diozero.weather.Forecast;

public class DataPointForecast extends Forecast<DataPointReport> {
	private DataPointForecastLocation location;
	private Map<String, Param> params;
	private String type;

	public DataPointForecast(long forecastTime, DataPointForecastLocation location, List<DataPointReport> reports,
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
	public DataPointForecastLocation getLocation() {
		return location;
	}
}
