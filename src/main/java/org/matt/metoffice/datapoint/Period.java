package org.matt.metoffice.datapoint;

import java.util.Date;
import java.util.List;

public class Period {
	private String type;
	private Date value;
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

	public Date getValue() {
		return value;
	}

	public void setValue(Date value) {
		this.value = value;
	}

	@Override
	public String toString() {
		return "Period [type=" + type + ", value=" + value + ", reports=" + reports + "]";
	}
}
