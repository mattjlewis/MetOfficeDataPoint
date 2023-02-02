package com.diozero.weather;

public enum WeatherType {
	// 2xx Thunderstorm
	/** thunderstorm - OWM: 2xx. DP: 30 */
	THUNDERSTORM,
	// 3xx Drizzle & 5xx Rain
	/** showers - OWM: 313, 520, 521, 522. DP: 9, 10 */
	RAIN_SHOWER,
	/** rain-light - OWM: 300, 301, 321, 500. DP: 11, 12 */
	LIGHT_RAIN,
	/** rain - OWM: 302, 311, 312, 314, 501, 502, 503, 504. DP: 13, 14, 15 */
	RAIN,
	/** rain-heavy - OWM: 531. DP: 28, 29 */
	RAIN_HEAVY,
	// 6xx Snow
	/** snow - OWM: 600, 601, 602, 620, 621, 622. DP: 22-27 */
	SNOW,
	/** sleet - OWM: 611, 612, 613, 615, 616. DP: 16, 17, 18 */
	SLEET,
	/** hail - OWM: 511. DP: 19, 20, 21 */
	HAIL,
	// 7xx Atmosphere
	/** ?fog? - OWM: 701. DP: 5 */
	MIST,
	/** fog - OWM: 741. DP: 6 */
	FOG,
	/** smoke - OWM: 711. DP: N/A */
	SMOKE,
	/** dust - OWM: 731. DP: N/A */
	DUST,
	/** windy - OWM: 771. DP: N/A */
	SQUALL,
	/**  */
	TORNADO, //
	// 80x Clouds
	/** day-sunny / night-clear - OWM: 800. DP: 0, 1 */
	CLEAR,
	/** cloudy-scattered - OWM: 801, 802. DP: 2, 3 */
	SCATTERED_CLOUDS,
	/** cloudy - OWM: 803. DP: 7 */
	CLOUDY,
	/** cloudy-overcast - OWM: 804. DP: 8 */
	OVERCAST, //
	;
}
