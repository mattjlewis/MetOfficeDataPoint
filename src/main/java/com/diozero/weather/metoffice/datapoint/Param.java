package com.diozero.weather.metoffice.datapoint;

public class Param {
	private String name;
	private String units;
	private String description;
	
	public Param() {
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getUnits() {
		return units;
	}

	public void setUnits(String units) {
		this.units = units;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	@Override
	public String toString() {
		return "Param [name=" + name + ", units=" + units + ", description=" + description + "]";
	}
}
