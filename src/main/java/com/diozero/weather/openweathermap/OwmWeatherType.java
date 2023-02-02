package com.diozero.weather.openweathermap;

/**
 * See https://openweathermap.org/weather-conditions#Weather-Condition-Codes-2
 */
public class OwmWeatherType {
	private int id;
	private String main;
	private String description;
	// https://openweathermap.org/img/wn/10d@2x.png
	private String icon;

	public OwmWeatherType(int id, String main, String description, String icon) {
		this.id = id;
		this.main = main;
		this.description = description;
		this.icon = icon;
	}

	public int getId() {
		return id;
	}

	public String getMain() {
		return main;
	}

	public String getDescription() {
		return description;
	}

	public String getIcon() {
		return icon;
	}

	@Override
	public String toString() {
		return "Weather [id=" + id + ", main=" + main + ", description=" + description + ", icon=" + icon + "]";
	}
}
