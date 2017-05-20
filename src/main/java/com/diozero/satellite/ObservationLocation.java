package com.diozero.satellite;

public class ObservationLocation {
	private double latitude;
	private double longitude;
	private double altitude;
	private String name;
	
	public ObservationLocation() {
		name = "";
	}
	
	public ObservationLocation(double latitude, double longitude, double altitude, String name) {
		this.latitude = latitude;
		this.longitude = longitude;
		this.altitude = altitude;
		this.name = name;
	}

	public double getLatitude() {
		return latitude;
	}

	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}

	public double getLongitude() {
		return longitude;
	}

	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}

	public double getAltitude() {
		return altitude;
	}

	public void setAltitude(double altitude) {
		this.altitude = altitude;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return "ObservationLocation [latitude=" + latitude + ", longitude=" + longitude + ", altitude=" + altitude
				+ ", name=" + name + "]";
	}
}
