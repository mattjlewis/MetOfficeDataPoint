package com.diozero.weather.openweather;

import java.util.Optional;

import com.diozero.weather.Report;

public class OwReport extends Report {
	public enum Type {
		CURRENT, MINUTELY, HOURLY, DAILY;
	}

	private OwReport.Type type;
	/** Epoch time in milliseconds */
	private long sunrise;
	/** Epoch time in milliseconds */
	private long sunset;
	/** Epoch time in milliseconds */
	private long moonrise;
	/** Epoch time in milliseconds */
	private long moonset;
	private double moonPhase;
	private Optional<Integer> seaLevelPressure;
	private Optional<Integer> groundLevelPressure;
	/** Degrees Celsius */
	private double dewPoint;
	/** Percentage */
	private int cloudiness;
	/** Rain volume for last period, mm */
	private double rainInLastPeriod;
	/** Snow volume for last period, mm */
	private double snowInLastPeriod;
	private OwWeatherType weatherType;

	public OwReport(OwReport.Type type, long epochTime, long sunrise, long sunset, long moonrise, long moonset,
			double temperature, double feelsLike, int pressure, Optional<Integer> seaLevelPressure,
			Optional<Integer> groundLevelPressure, int humidity, double dewPoint, double uvIndex, int cloudiness,
			int visibility, double rainProbability, double windSpeed, double windGusts, int windDirection,
			double rainInLastPeriod, double snowInLastPeriod, OwWeatherType weatherType, double moonPhase) {
		super(epochTime, temperature, feelsLike, pressure, humidity, uvIndex, windSpeed, windGusts, windDirection,
				visibility, (int) Math.round(rainProbability * 100));

		this.type = type;
		this.sunrise = sunrise;
		this.sunset = sunset;
		this.moonrise = moonrise;
		this.moonset = moonset;
		this.seaLevelPressure = seaLevelPressure;
		this.groundLevelPressure = groundLevelPressure;
		this.dewPoint = dewPoint;
		this.cloudiness = cloudiness;
		this.rainInLastPeriod = rainInLastPeriod;
		this.snowInLastPeriod = snowInLastPeriod;
		this.weatherType = weatherType;
		this.moonPhase = moonPhase;
	}

	public OwReport.Type getType() {
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

	public double getMoonPhase() {
		return moonPhase;
	}

	public Optional<Integer> getSeaLevelPressure() {
		return seaLevelPressure;
	}

	public Optional<Integer> getGroundLevelPressure() {
		return groundLevelPressure;
	}

	public double getDewPoint() {
		return dewPoint;
	}

	public int getCloudiness() {
		return cloudiness;
	}

	public double getRainInLastPeriod() {
		return rainInLastPeriod;
	}

	public double getSnowInLastPeriod() {
		return snowInLastPeriod;
	}

	public OwWeatherType getWeatherType() {
		return weatherType;
	}

	@Override
	public String toString() {
		return "OwReport [type=" + type + ", sunrise=" + sunrise + ", sunset=" + sunset + ", moonrise=" + moonrise
				+ ", moonset=" + moonset + ", seaLevelPressure=" + seaLevelPressure + ", groundLevelPressure="
				+ groundLevelPressure + ", dewPoint=" + dewPoint + ", cloudiness=" + cloudiness + ", rainInLastPeriod="
				+ rainInLastPeriod + ", snowInLastPeriod=" + snowInLastPeriod + ", weatherType=" + weatherType
				+ ", moonPhase=" + moonPhase + ", report: " + super.toString() + "]";
	}
}
