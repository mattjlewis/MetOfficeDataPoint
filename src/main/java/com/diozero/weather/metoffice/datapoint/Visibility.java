package com.diozero.weather.metoffice.datapoint;

public enum Visibility {
	EX("Excellent - More than 40 km", 60_000), VG("Very Good - Between 20-40 km", 30_000),
	GO("Good - Between 10-20 km", 15_000), MO("Moderate - Between 4-10 km", 7_000), PO("Poor - Between 1-4 km", 2_500),
	PV("Very Poor - Less than 1 km", 500);

	private String label;
	private int distance;

	Visibility(String label, int distance) {
		this.label = label;
		this.distance = distance;
	}

	public String getLabel() {
		return label;
	}

	public int getDistance() {
		return distance;
	}
}
