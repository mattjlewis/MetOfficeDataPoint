package com.diozero.weather.metoffice.datapoint;

import com.diozero.location.GeographicLocation;

public class DpForecastLocation extends GeographicLocation {
	private int id;
	private String nationalPark;
	private String region;
	private String unitaryAuthArea;
	private String country;
	private String continent;
	private String obsSource;

	public DpForecastLocation(double latitude, double longitude, double altitude, String name, int id,
			String nationalPark, String region, String unitaryAuthArea, String country, String continent,
			String obsSource) {
		super(latitude, longitude, altitude, name, country);

		this.id = id;
		this.nationalPark = nationalPark;
		this.region = region;
		this.unitaryAuthArea = unitaryAuthArea;
		this.country = country;
		this.continent = continent;
		this.obsSource = obsSource;
	}

	public int getId() {
		return id;
	}

	public String getNationalPark() {
		return nationalPark;
	}

	public String getRegion() {
		return region;
	}

	public String getRegionValue() {
		return UKRegionMapping.VALUES_MAP.get(region);
	}

	public String getUnitaryAuthArea() {
		return unitaryAuthArea;
	}

	public String getCountry() {
		return country;
	}

	public String getContinent() {
		return continent;
	}

	public String getObsSource() {
		return obsSource;
	}

	@Override
	public String toString() {
		return "DataPointForecastLocation [id=" + id + ", nationalPark=" + nationalPark + ", region=" + region
				+ ", unitaryAuthArea=" + unitaryAuthArea + ", country=" + country + ", continent=" + continent
				+ ", obsSource=" + obsSource + ", latitude=" + getLatitude() + ", longitude=" + getLongitude()
				+ ", altitude=" + getAltitude() + ", cityName=" + getCityName() + ", countryCode=" + getCountryCode()
				+ "]";
	}
}
