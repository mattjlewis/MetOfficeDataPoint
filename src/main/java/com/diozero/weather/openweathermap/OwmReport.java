package com.diozero.weather.openweathermap;

import com.diozero.weather.Report;

public class OwmReport extends Report {
	public enum Type {
		CURRENT, MINUTELY, HOURLY, DAILY;
	}

	private OwmReport.Type type;
	/** Epoch time in milliseconds */
	private long sunrise;
	/** Epoch time in milliseconds */
	private long sunset;
	/** Epoch time in milliseconds */
	private long moonrise;
	/** Epoch time in milliseconds */
	private long moonset;
	/** Degrees Celsius */
	private double dewPoint;
	/** Percentage */
	private int cloudiness;
	/** Rain volume for last hour, mm */
	private double rainInLastHour;
	/** Snow volume for last hour, mm */
	private double snowInLastHour;
	private OwmWeatherType weather;
	private double moonPhase;

	public OwmReport(OwmReport.Type type, long epochTime, long sunrise, long sunset, long moonrise, long moonset,
			double temperature, double feelsLike, int pressure, int humidity, double dewPoint, double uvIndex,
			int cloudiness, int visibility, double rainProbability, double windSpeed, double windGusts,
			int windDirection, double rainInLastHour, double snowInLastHour, OwmWeatherType weather, double moonPhase) {
		super(epochTime, temperature, feelsLike, pressure, humidity, uvIndex, windSpeed, windGusts, windDirection,
				visibility, (int) Math.round(rainProbability * 100));

		this.type = type;
		this.sunrise = sunrise;
		this.sunset = sunset;
		this.moonrise = moonrise;
		this.moonset = moonset;
		this.dewPoint = dewPoint;
		this.cloudiness = cloudiness;
		this.rainInLastHour = rainInLastHour;
		this.snowInLastHour = snowInLastHour;
		this.weather = weather;
		this.moonPhase = moonPhase;
	}

	public OwmReport.Type getType() {
		return type;
	}

	public long getSunrise() {
		return sunrise;
	}

	public long getSunset() {
		return sunset;
	}

	public long getMoonrise() {
		return moonrise;
	}

	public long getMoonset() {
		return moonset;
	}

	public double getDewPoint() {
		return dewPoint;
	}

	public int getCloudiness() {
		return cloudiness;
	}

	public double getRainInLastHour() {
		return rainInLastHour;
	}

	public double getSnowInLastHour() {
		return snowInLastHour;
	}

	public OwmWeatherType getWeather() {
		return weather;
	}

	public double getMoonPhase() {

		return moonPhase;
	}
}
