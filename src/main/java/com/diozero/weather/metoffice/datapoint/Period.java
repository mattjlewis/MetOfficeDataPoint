package com.diozero.weather.metoffice.datapoint;

import java.time.ZonedDateTime;
import java.util.List;

public class Period {
	private String type;
	private ZonedDateTime value;
	private List<Report> reports;
	
	public Period() {
	}

	public List<Report> getReports() {
		return reports;
	}

	public void setReports(List<Report> reports) {
		this.reports = reports;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public ZonedDateTime getValue() {
		return value;
	}

	public void setValue(ZonedDateTime value) {
		this.value = value;
	}

	@Override
	public String toString() {
		return "Period [type=" + type + ", value=" + value + ", reports=" + reports + "]";
	}
}
