package com.diozero.weather.metoffice.datapoint;

public enum DpWeatherType {
	_0("Clear night"), _1("Sunny day"), _2("Partly cloudy (night)"), _3("Partly cloudy (day)"), _4("Not used"),
	_5("Mist"), _6("Fog"), _7("Cloudy"), _8("Overcast"), _9("Light rain shower (night)"),
	_10("Light rain shower (day)"), _11("Drizzle"), _12("Light rain"), _13("Heavy rain shower (night)"),
	_14("Heavy rain shower (day)"), _15("Heavy rain"), _16("Sleet shower (night)"), _17("Sleet shower (day)"),
	_18("Sleet"), _19("Hail shower (night)"), _20("Hail shower (day)"), _21("Hail"), _22("Light snow shower (night)"),
	_23("Light snow shower (day)"), _24("Light snow"), _25("Heavy snow shower (night)"), _26("Heavy snow shower (day)"),
	_27("Heavy snow"), _28("Thunder shower (night)"), _29("Thunder shower (day)"), _30("Thunder"), NA("Not available"),;

	private String label;

	DpWeatherType(String label) {
		this.label = label;
	}

	public String getLabel() {
		return label;
	}

	@Override
	public String toString() {
		return name() + " (" + label + ")";
	}
}
