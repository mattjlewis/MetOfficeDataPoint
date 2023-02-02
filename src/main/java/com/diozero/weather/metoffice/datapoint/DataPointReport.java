package com.diozero.weather.metoffice.datapoint;

import java.time.ZonedDateTime;

import com.diozero.util.DirectionUtil;
import com.diozero.weather.Report;

public class DataPointReport extends Report {
	private ZonedDateTime periodStart;
	private int minutesAfterMidnight;

	private DataPointWeatherType weatherType;
	private Visibility visibility;

	public DataPointReport(ZonedDateTime periodStart, int minutesAfterMidnight, DataPointWeatherType weatherType,
			Visibility visibility, int temperature, int feelsLike, int pressure, int windSpeed, int windGust,
			String windDirection, int precipitationProbability, int relativeHumidity, int maximumUvIndex) {
		super(periodStart.toInstant().toEpochMilli() + minutesAfterMidnight * 60 * 1_000, temperature, feelsLike,
				pressure, relativeHumidity, maximumUvIndex, windSpeed, windGust,
				DirectionUtil.getDirectionDeg(windDirection), visibility.getDistance(), precipitationProbability);

		this.periodStart = periodStart;
		this.minutesAfterMidnight = minutesAfterMidnight;
		this.weatherType = weatherType;
		this.visibility = visibility;
	}

	public ZonedDateTime getPeriodStart() {
		return periodStart;
	}

	public int getMinutesAfterMidnight() {
		return minutesAfterMidnight;
	}

	public DataPointWeatherType getWeatherTypeValue() {
		return weatherType;
	}

	public String getWeatherTypeCode() {
		return weatherType.toString();
	}

	public String getWeatherTypeLabel() {
		return weatherType.getLabel();
	}

	public String getVisibilityLabel() {
		return visibility.getLabel();
	}

	@Override
	public String toString() {
		return "DataPointReport [" + super.toString() + ", periodStart=" + periodStart + ", minutesAfterMidnight="
				+ minutesAfterMidnight + ", weatherType=" + weatherType + ", visibility=" + visibility + "]";
	}
}
