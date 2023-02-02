package com.diozero.satellite;

import java.time.ZonedDateTime;
import java.util.concurrent.TimeUnit;

import com.diozero.util.DirectionUtil;

public class SatelliteFlyby {
	private String name;
	private ZonedDateTime approachDateTime;
	private int approachElevation;
	private int approachDirectionDeg;
	private String approachDirectionCompass;
	private ZonedDateTime peakDateTime;
	private int peakElevation;
	private int peakDirectionDeg;
	private String peakDirectionCompass;
	private int departureElevation;
	private int departureDirectionDeg;
	private String departureDirectionCompass;
	private ZonedDateTime departureDateTime;
	private int durationSeconds;

	public SatelliteFlyby(ZonedDateTime approachDateTime, int approachElevation, String approachDirectionCompass,
			int peakElevation, ZonedDateTime departureDateTime, int departureElevation,
			String departureDirectionCompass) {
		this.peakElevation = peakElevation;
		this.approachDateTime = approachDateTime;
		this.approachElevation = approachElevation;
		this.approachDirectionCompass = approachDirectionCompass;
		this.departureDateTime = departureDateTime;
		this.departureElevation = departureElevation;
		this.departureDirectionCompass = departureDirectionCompass;

		approachDirectionDeg = DirectionUtil.getDirectionDeg(approachDirectionCompass);
		departureDirectionDeg = DirectionUtil.getDirectionDeg(departureDirectionCompass);
		durationSeconds = (int) (departureDateTime.toEpochSecond() - approachDateTime.toEpochSecond());
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public SatelliteFlyby(ZonedDateTime approachDateTime, int approachElevation, int approachDirectionDeg,
			ZonedDateTime peakDateTime, int peakElevation, int peakDirectionDeg, ZonedDateTime departureDateTime,
			int departureElevation, int departureDirectionDeg) {
		this.approachDateTime = approachDateTime;
		this.approachElevation = approachElevation;
		this.approachDirectionDeg = approachDirectionDeg;
		this.peakDateTime = peakDateTime;
		this.peakElevation = peakElevation;
		this.peakDirectionDeg = peakDirectionDeg;
		this.departureDateTime = departureDateTime;
		this.departureElevation = departureElevation;
		this.departureDirectionDeg = departureDirectionDeg;

		approachDirectionCompass = DirectionUtil.getDirectionString(approachDirectionDeg);
		peakDirectionCompass = DirectionUtil.getDirectionString(peakDirectionDeg);
		departureDirectionCompass = DirectionUtil.getDirectionString(departureDirectionDeg);
		durationSeconds = (int) (departureDateTime.toEpochSecond() - approachDateTime.toEpochSecond());
	}

	public ZonedDateTime getApproachDateTime() {
		return approachDateTime;
	}

	public int getApproachElevation() {
		return approachElevation;
	}

	public int getApproachDirectionDeg() {
		return approachDirectionDeg;
	}

	public String getApproachDirectionCompass() {
		return approachDirectionCompass;
	}

	public ZonedDateTime getPeakDateTime() {
		return peakDateTime;
	}

	public int getPeakElevation() {
		return peakElevation;
	}

	public int getPeakDirectionDeg() {
		return peakDirectionDeg;
	}

	public String getPeakDirectionCompass() {
		return peakDirectionCompass;
	}

	public ZonedDateTime getDepartureDateTime() {
		return departureDateTime;
	}

	public int getDepartureElevation() {
		return departureElevation;
	}

	public int getDepartureDirectionDeg() {
		return departureDirectionDeg;
	}

	public String getDepartureDirectionCompass() {
		return departureDirectionCompass;
	}

	public String getDurationMinutes() {
		int mins = (int) TimeUnit.SECONDS.toMinutes(durationSeconds);
		int secs = durationSeconds - mins * 60;
		return String.format("%02d:%02d", Integer.valueOf(mins), Integer.valueOf(secs));
	}

	@Override
	public String toString() {
		return "SatelliteFlyby [approachDateTime=" + approachDateTime + ", approachElevation=" + approachElevation
				+ ", approachDirectionDeg=" + approachDirectionDeg + ", approachDirectionCompass="
				+ approachDirectionCompass + ", peakDateTime=" + peakDateTime + ", peakElevation=" + peakElevation
				+ ", peakDirectionDeg=" + peakDirectionDeg + ", peakDirectionCompass=" + peakDirectionCompass
				+ ", departureElevation=" + departureElevation + ", departureDirectionDeg=" + departureDirectionDeg
				+ ", departureDirectionCompass=" + departureDirectionCompass + ", departureDateTime="
				+ departureDateTime + ", durationSeconds=" + durationSeconds + "]";
	}
}
