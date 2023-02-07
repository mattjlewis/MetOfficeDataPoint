package com.diozero.weather.openweather;

/**
 * See https://openweathermap.org/weather-conditions#Weather-Condition-Codes-2
 */
public class OwWeatherType {
	private static final String ICON_BASE_FORMAT = "https://openweathermap.org/img/wn/%s.png";
	private static final String ICON_BASE_SCALE_FORMAT = "https://openweathermap.org/img/wn/%s@%dx.png";

	public static final int CLEAR_SKY = 800;
	public static final String CLEAR_SKY_NIGHT_ICON = "01n";

	private int id;
	private String main;
	private String description;
	// e.g."01n" - translate to "https://openweathermap.org/img/wn/01n.png",
	// "https://openweathermap.org/img/wn/01n@2x.png"
	private String icon;

	public OwWeatherType(int id, String main, String description, String icon) {
		this.id = id;
		this.main = main;
		this.description = description;
		this.icon = icon;
	}

	public int getId() {
		return id;
	}

	public String getMain() {
		return main;
	}

	public String getDescription() {
		return description;
	}

	public String getIcon() {
		return icon;
	}

	public String getIconUri() {
		return String.format(ICON_BASE_FORMAT, icon);
	}

	public String getIconUriX2() {
		return getIconUriScaled(2);
	}

	public String getIconUriX4() {
		return getIconUriScaled(4);
	}

	private String getIconUriScaled(int scale) {
		return String.format(ICON_BASE_SCALE_FORMAT, icon, Integer.valueOf(scale));
	}

	@Override
	public String toString() {
		return "Weather [id=" + id + ", main=" + main + ", description=" + description + ", icon=" + icon + "]";
	}
}
