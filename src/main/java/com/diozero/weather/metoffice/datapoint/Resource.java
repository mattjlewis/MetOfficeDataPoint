package org.matt.metoffice.datapoint;

import java.util.Date;
import java.util.List;

public class Resource {
	private Date dataDate;
	private String res;
	private String type;
	private List<Date> timeSteps;
	
	public Resource() {
	}

	public Date getDataDate() {
		return dataDate;
	}

	public void setDataDate(Date dataDate) {
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

	public List<Date> getTimesteps() {
		return timeSteps;
	}

	public void setTimeSteps(List<Date> timeSteps) {
		this.timeSteps = timeSteps;
	}

	@Override
	public String toString() {
		return "Resource [dataDate=" + dataDate + ", res=" + res + ", type=" + type + ", timeSteps=" + timeSteps + "]";
	}
}
