package com.diozero.satellite;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.diozero.util.DirectionUtil;

@SuppressWarnings("static-method")
public class DirectionStringTest {
	@Test
	public void test() {
		for (DirectionUtil.Direction dir : DirectionUtil.Direction.values()) {
			System.out.println(dir + ": " + dir.getAngle());
		}

		for (float angle = 0; angle <= 360; angle += (360 / 32f)) {
			int int_angle = Math.round(angle);
			System.out.println(int_angle + " = " + DirectionUtil.getDirection(int_angle));
		}

		Assertions.assertEquals(DirectionUtil.Direction.N, DirectionUtil.getDirection(0));
		Assertions.assertEquals(DirectionUtil.Direction.E, DirectionUtil.getDirection(90));
		Assertions.assertEquals(DirectionUtil.Direction.S, DirectionUtil.getDirection(180));
		Assertions.assertEquals(DirectionUtil.Direction.W, DirectionUtil.getDirection(270));
		Assertions.assertEquals(DirectionUtil.Direction.N, DirectionUtil.getDirection(360));
		Assertions.assertEquals(DirectionUtil.Direction.N, DirectionUtil.getDirection(11));
		Assertions.assertEquals(DirectionUtil.Direction.NNE, DirectionUtil.getDirection(12));
		Assertions.assertEquals(DirectionUtil.Direction.NNE, DirectionUtil.getDirection(23));
		Assertions.assertEquals(DirectionUtil.Direction.NNW, DirectionUtil.getDirection(360 - 22));
		Assertions.assertEquals(DirectionUtil.Direction.N, DirectionUtil.getDirection(360 - 11));

		String dir = "Fred";
		try {
			DirectionUtil.getDirectionDeg(dir);
			Assertions.fail("Error should have been thrown for invalid direction '" + dir + "'");
		} catch (IllegalArgumentException e) {
			// Expected, ignore
		}
	}
}
