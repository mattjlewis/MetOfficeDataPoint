package com.diozero.weather;

public class Report {
	/** Epoch time in milliseconds */
	private long epochTime;
	/** Degrees Celsius */
	private double temperature;
	/** Degrees Celsius */
	private double feelsLike;
	/** Hectopascals (hPa) (1 hPa == 100 Pa == 1 millibar) */
	private int pressure;
	/** Percentage relative humidity (0..100) */
	private int relativeHumidity;
	/** Current UV index (0..10) */
	private double uvIndex;
	/** Metres / second */
	private double windSpeed;
	/** Metres / second */
	private double windGusts;
	/** Degrees */
	private int windDirection;
	/** Metres */
	private int visibility;
	/** 0..100 */
	private int rainProbability;

	public Report(long epochTime, double temperature, double feelsLike, int pressure, int relativeHumidity,
			double uvIndex, double windSpeed, double windGusts, int windDirection, int visibility,
			int rainProbability) {
		this.epochTime = epochTime;
		this.temperature = temperature;
		this.feelsLike = feelsLike;
		this.pressure = pressure;
		this.relativeHumidity = relativeHumidity;
		this.uvIndex = uvIndex;
		this.windSpeed = windSpeed;
		this.windGusts = windGusts;
		this.windDirection = windDirection;
		this.visibility = visibility;
		this.rainProbability = rainProbability;
	}

	public long getEpochTime() {
		return epochTime;
	}

	public double getTemperature() {
		return temperature;
	}

	public double getFeelsLike() {
		return feelsLike;
	}

	public int getPressure() {
		return pressure;
	}

	public int getRelativeHumidity() {
		return relativeHumidity;
	}

	public double getUvIndex() {
		return uvIndex;
	}

	public double getWindSpeed() {
		return windSpeed;
	}

	public double getWindGusts() {
		return windGusts;
	}

	public int getWindDirection() {
		return windDirection;
	}

	public int getVisibility() {
		return visibility;
	}

	public int getRainProbability() {
		return rainProbability;
	}

	@Override
	public String toString() {
		return "Report [epochTime=" + epochTime + ", temperature=" + temperature + ", feelsLike=" + feelsLike
				+ ", pressure=" + pressure + ", relativeHumidity=" + relativeHumidity + ", uvIndex=" + uvIndex
				+ ", windSpeed=" + windSpeed + ", windGusts=" + windGusts + ", windDirection=" + windDirection
				+ ", visibility=" + visibility + ", rainProbability=" + rainProbability + "]";
	}
}
