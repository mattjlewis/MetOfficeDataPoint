package org.matt.metoffice.datapoint;

import java.util.*;

public class Report {
	private int minutesAfterMidnight;
	private Date periodStart;
	private Calendar reportDateTime;
	
	private Map<String, Object> values;
	public static final String MAX_UV_INDEX = "U";
	private int maximumUvIndex;
	public static final String WEATHER_TYPE = "W";
	private String weatherType;
	public static final String VISIBILITY = "V";
	private String visibility;
	public static final String TEMPERATURE = "T";
	private int temperature;
	public static final String WIND_SPEED = "S";
	private int windSpeed;
	public static final String PRECIPITATION_PROBABILITY = "Pp";
	private int precipitationProbability;
	public static final String SCREEN_RELATIVE_HUMIDITY = "H";
	private int screenRelativeHumidity;
	public static final String WIND_GUST = "G";
	private int windGust;
	public static final String FEELS_LIKE_TEMPERATURE = "F";
	private int feelsLikeTemperature;
	public static final String WIND_DIRECTION = "D";
	private String windDirection;
	
	public Report(Date periodStart) {
		values = new HashMap<>();
		this.periodStart = periodStart;
	}

	public int getMinutesAfterMidnight() {
		return minutesAfterMidnight;
	}

	public void setMinutesAfterMidnight(int minutesAfterMidnight) {
		this.minutesAfterMidnight = minutesAfterMidnight;
		reportDateTime = Calendar.getInstance();
		reportDateTime.setTime(periodStart);
		reportDateTime.add(Calendar.MINUTE, minutesAfterMidnight);
	}
	
	public Date getReportDateTime() {
		return reportDateTime.getTime();
	}
	
	public Map<String, Object> getValues() {
		return values;
	}
	
	public int getMaximumUvIndex() {
		return maximumUvIndex;
	}
	
	public void setMaximumUvIndex(int maximumUvIndex) {
		this.maximumUvIndex = maximumUvIndex;
		values.put(MAX_UV_INDEX, Integer.valueOf(this.maximumUvIndex));
	}
	
	public String getWeatherType() {
		return weatherType;
	}
	
	public void setWeatherType(String weatherType) {
		this.weatherType = weatherType;
		values.put(WEATHER_TYPE, WeatherCodes.VALUES_MAP.get(this.weatherType));
	}
	
	public String getVisibility() {
		return visibility;
	}
	
	public String getVisibilityValue() {
		return VisibilityCodes.VALUES_MAP.get(visibility);
	}
	
	public void setVisibility(String visibility) {
		this.visibility = visibility;
		values.put(VISIBILITY, VisibilityCodes.VALUES_MAP.get(this.visibility));
	}
	
	public int getTemperature() {
		return temperature;
	}
	
	public void setTemperature(int temperature) {
		this.temperature = temperature;
		values.put(TEMPERATURE, Integer.valueOf(this.temperature));
	}
	
	public int getWindSpeed() {
		return windSpeed;
	}
	
	public void setWindSpeed(int windSpeed) {
		this.windSpeed = windSpeed;
		values.put(WIND_SPEED, Integer.valueOf(this.windSpeed));
	}
	
	public int getPrecipitationProbability() {
		return precipitationProbability;
	}
	
	public void setPrecipitationProbability(int precipitationProbability) {
		this.precipitationProbability = precipitationProbability;
		values.put(PRECIPITATION_PROBABILITY, Integer.valueOf(this.precipitationProbability));
	}
	
	public int getScreenRelativeHumidity() {
		return screenRelativeHumidity;
	}
	
	public void setScreenRelativeHumidity(int screenRelativeHumidity) {
		this.screenRelativeHumidity = screenRelativeHumidity;
		values.put(SCREEN_RELATIVE_HUMIDITY, Integer.valueOf(this.screenRelativeHumidity));
	}
	
	public int getWindGust() {
		return windGust;
	}
	
	public void setWindGust(int windGust) {
		this.windGust = windGust;
		values.put(WIND_GUST, Integer.valueOf(this.windGust));
	}
	
	public int getFeelsLikeTemperature() {
		return feelsLikeTemperature;
	}
	
	public void setFeelsLikeTemperature(int feelsLikeTemperature) {
		this.feelsLikeTemperature = feelsLikeTemperature;
		values.put(FEELS_LIKE_TEMPERATURE, Integer.valueOf(this.feelsLikeTemperature));
	}
	
	public String getWindDirection() {
		return windDirection;
	}
	
	public void setWindDirection(String windDirection) {
		this.windDirection = windDirection;
		values.put(WIND_DIRECTION, this.windDirection);
	}
	
	@Override
	public String toString() {
		return "Report [minutesAfterMidnight=" + minutesAfterMidnight + "maximumUvIndex=" + maximumUvIndex +
				", weatherType=" + weatherType + ", visibility=" + visibility + ", temperature=" +
				temperature + ", windSpeed=" + windSpeed + ", precipitationProbability=" +
				precipitationProbability + ", screenRelativeHumidity=" + screenRelativeHumidity +
				", windGust=" + windGust + ", feelsLikeTemperature=" + feelsLikeTemperature
				+ ", windDirection=" + windDirection + "]";
	}
}
