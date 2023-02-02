package com.diozero.util;

public class DirectionUtil {
	/**
	 * <pre>
	 *              (0)
	 *               N
	 *   (337.5)  NNW NNE (22.5)
	 *    (315) NW       NE (45)
	 * (292.5) WNW       ENE (67.5)
	 *  (270) W             E (90)
	 * (247.5) WSW       ESE (112.5)
	 *    (225) SW       SE (135)
	 *   (202.5)  SSW SSE (157.5)
	 *               S
	 *             (180)
	 * </pre>
	 */
	public enum Direction {
		N(0), NNE(1), NE(2), ENE(3), E(4), ESE(5), SE(6), SSE(7), S(8), SSW(9), SW(10), WSW(11), W(12), WNW(13), NW(14),
		NNW(15);

		private int angle;

		Direction(int index) {
			angle = Math.round(index * 360f / 16);
		}

		public int getAngle() {
			return angle;
		}
	}

	public static Direction getDirection(int angle) {
		return Direction.values()[Math.round(angle / (360f / Direction.values().length)) % Direction.values().length];
	}

	public static Direction getDirection(String compassDir) {
		return Direction.valueOf(compassDir);
	}

	public static String getDirectionString(int angle) {
		return getDirection(angle).toString();
	}

	public static int getDirectionDeg(String compassDir) {
		return getDirection(compassDir).getAngle();
	}
}
