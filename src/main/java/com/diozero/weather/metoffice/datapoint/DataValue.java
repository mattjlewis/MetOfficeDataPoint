package com.diozero.weather.metoffice.datapoint;

import java.time.ZonedDateTime;

public class DataValue {
	private ZonedDateTime dataDate;
	private String type;
	private ForecastLocation location;
	
	public DataValue() {
	}

	public ZonedDateTime getDataDate() {
		return dataDate;
	}

	public void setDataDate(ZonedDateTime dataDate) {
		this.dataDate = dataDate;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public ForecastLocation getLocation() {
		return location;
	}

	public void setLocation(ForecastLocation location) {
		this.location = location;
	}

	@Override
	public String toString() {
		return "DataValue [dataDate=" + dataDate + ", type=" + type + ", location=" + location + "]";
	}
}
