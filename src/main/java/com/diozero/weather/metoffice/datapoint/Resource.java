package com.diozero.weather.metoffice.datapoint;

import java.time.ZonedDateTime;
import java.util.List;

public class Resource {
	private ZonedDateTime dataDate;
	private String res;
	private String type;
	private List<ZonedDateTime> timeSteps;
	
	public Resource() {
	}

	public ZonedDateTime getDataDate() {
		return dataDate;
	}

	public void setDataDate(ZonedDateTime dataDate) {
		this.dataDate = dataDate;
	}

	public String getRes() {
		return res;
	}

	public void setRes(String res) {
		this.res = res;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public List<ZonedDateTime> getTimesteps() {
		return timeSteps;
	}

	public void setTimeSteps(List<ZonedDateTime> timeSteps) {
		this.timeSteps = timeSteps;
	}

	@Override
	public String toString() {
		return "Resource [dataDate=" + dataDate + ", res=" + res + ", type=" + type + ", timeSteps=" + timeSteps + "]";
	}
}
