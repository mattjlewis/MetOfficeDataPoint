package com.diozero.weather.metoffice.datapoint;

public enum Visibility {
	UNKNOWN("Unknown", -1, -1), VP("Very Poor - Less than 1 km", 0, 1_000), PO("Poor - Between 1-4 km", 1_000, 4_000),
	MO("Moderate - Between 4-10 km", 4_000, 10_000), GO("Good - Between 10-20 km", 10_000, 20_000),
	VG("Very Good - Between 20-40 km", 20_000, 40_000), EX("Excellent - More than 40 km", 40_000, Integer.MAX_VALUE);

	private String label;
	private int minDistance;
	private int maxDistance;

	Visibility(String label, int minDistance, int maxDistance) {
		this.label = label;
		this.minDistance = minDistance;
		this.maxDistance = maxDistance;
	}

	public String getLabel() {
		return label;
	}

	public int getMinDistance() {
		return minDistance;
	}

	public int getMaxDistance() {
		return maxDistance;
	}

	@Override
	public String toString() {
		return name() + " (" + label + ")";
	}
}
