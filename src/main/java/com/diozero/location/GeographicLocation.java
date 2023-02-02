package com.diozero.location;

public class GeographicLocation {
	/** Latitude in degrees */
	private double latitude;
	/** Longitude in degrees */
	private double longitude;
	/** Altitude in meters */
	private double altitude;
	/** The name of this location */
	private String cityName;
	private String countryCode;

	public GeographicLocation(double latitude, double longitude, double altitude) {
		this(latitude, longitude, altitude, "Unknown", "Unknown");
	}

	public GeographicLocation(double latitude, double longitude, double altitude, String cityName, String countryCode) {
		/*-
		double lat = MathUtils.normalizeAngle(latitude, FastMath.PI / 2);
		double lon = MathUtils.normalizeAngle(longitude, 0);
		if (lat > FastMath.PI / 2.0) {
			// latitude is beyond the pole -> add 180 to longitude
			lat = FastMath.PI - lat;
			lon = MathUtils.normalizeAngle(longitude + FastMath.PI, 0);
		}
		*/
		double lat = latitude;
		double lon = longitude;

		this.latitude = lat;
		this.longitude = lon;
		this.altitude = altitude;
		this.cityName = cityName;
		this.countryCode = countryCode;
	}

	public double getLatitude() {
		return latitude;
	}

	public double getLongitude() {
		return longitude;
	}

	public double getAltitude() {
		return altitude;
	}

	public String getCityName() {
		return cityName;
	}

	public String getCountryCode() {
		return countryCode;
	}

	@Override
	public String toString() {
		return "ObservationLocation [latitude=" + latitude + ", longitude=" + longitude + ", altitude=" + altitude
				+ ", cityName=" + cityName + ", countryCode=" + countryCode + "]";
	}

	public double distance(GeographicLocation p2) {
		return distance(this, p2);
	}

	/**
	 * Calculate distance between two points in latitude and longitude taking into
	 * account height difference. If you are not interested in height difference
	 * pass 0.0. Uses Haversine method as its base.
	 *
	 * lat1, lon1 Start point lat2, lon2 End point el1 Start altitude in meters el2
	 * End altitude in meters
	 *
	 * @param p1 point 1
	 * @param p2 point 2
	 * @returns Distance in Meters
	 */
	public static double distance(GeographicLocation p1, GeographicLocation p2) {
		final int R = 6371; // Radius of the earth

		double lat_distance_rad = Math.toRadians(p2.getLatitude() - p1.getLatitude());
		double lon_distance_rad = Math.toRadians(p2.getLongitude() - p1.getLongitude());
		double a = Math.sin(lat_distance_rad / 2) * Math.sin(lat_distance_rad / 2)
				+ Math.cos(Math.toRadians(p1.getLatitude())) * Math.cos(Math.toRadians(p2.getLatitude()))
						* Math.sin(lon_distance_rad / 2) * Math.sin(lon_distance_rad / 2);
		double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
		double distance = R * c * 1000; // convert to meters

		double height = p1.getAltitude() - p2.getAltitude();

		distance = Math.pow(distance, 2) + Math.pow(height, 2);

		return Math.sqrt(distance);
	}
}
