package org.matt.metoffice.datapoint;

import java.util.Date;

public class DataValue {
	private Date dataDate;
	private String type;
	private Location location;
	
	public DataValue() {
	}

	public Date getDataDate() {
		return dataDate;
	}

	public void setDataDate(Date dataDate) {
		this.dataDate = dataDate;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public Location getLocation() {
		return location;
	}

	public void setLocation(Location location) {
		this.location = location;
	}

	@Override
	public String toString() {
		return "DataValue [dataDate=" + dataDate + ", type=" + type + ", location=" + location + "]";
	}
}
