package org.matt.metoffice.datapoint;

import java.util.List;

import com.spatial4j.core.shape.Point;

public class Location {
	private int id;
	private Float elevation;
	private float latitude;
	private float longitude;
	private Point point;
	private String name;
	private String nationalPark;
	private String region;
	private String unitaryAuthArea;
	private String country;
	private String continent;
	private List<Period> periods;
	
	public Location() {
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public Float getElevation() {
		return elevation;
	}

	public void setElevation(Float elevation) {
		this.elevation = elevation;
	}

	public float getLatitude() {
		return latitude;
	}

	public void setLatitude(float latitude) {
		this.latitude = latitude;
	}

	public float getLongitude() {
		return longitude;
	}

	public void setLongitude(float longitude) {
		this.longitude = longitude;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getNationalPark() {
		return nationalPark;
	}

	public void setNationalPark(String nationalPark) {
		this.nationalPark = nationalPark;
	}

	public String getRegion() {
		return region;
	}

	public void setRegion(String region) {
		this.region = region;
	}

	public String getUnitaryAuthArea() {
		return unitaryAuthArea;
	}

	public void setUnitaryAuthArea(String unitaryAuthArea) {
		this.unitaryAuthArea = unitaryAuthArea;
	}

	public List<Period> getPeriods() {
		return periods;
	}

	public void setPeriods(List<Period> periods) {
		this.periods = periods;
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public String getContinent() {
		return continent;
	}

	public void setContinent(String continent) {
		this.continent = continent;
	}

	@Override
	public String toString() {
		return "Location [id=" + id + ", elevation=" + elevation + ", latitude=" + latitude + ", longitude=" + longitude
				+ ", name=" + name + ", nationalPark=" + nationalPark + ", region=" + region + ", unitaryAuthArea="
				+ unitaryAuthArea + ", periods=" + periods + ", country=" + country + ", continent=" + continent + "]";
	}

	public Point getPoint() {
		return point;
	}

	public void setPoint(Point point) {
		this.point = point;
	}
}
