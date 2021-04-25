package com.diozero.satellite;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.diozero.satellite.orekit.SatelliteUtil;

@SuppressWarnings("static-method")
public class DirectionStringTest {
	@Test
	public void test() {
		for (double angle=0; angle<=360; angle+=(360/32.0)) {
			String dir = SatelliteUtil.getDirectionString((int) angle);
			System.out.println(angle + " = " + dir);
		}
		
		int angle = 0;
		String dir = SatelliteUtil.getDirectionString(angle);
		Assertions.assertEquals("N", dir);
		
		angle = 11;
		dir = SatelliteUtil.getDirectionString(angle);
		Assertions.assertEquals("N", dir);
		
		angle = 360-11;
		dir = SatelliteUtil.getDirectionString(angle);
		Assertions.assertEquals("N", dir);
		
		angle = 12;
		dir = SatelliteUtil.getDirectionString(angle);
		Assertions.assertEquals("NNE", dir);
		
		angle = 90;
		dir = SatelliteUtil.getDirectionString(angle);
		Assertions.assertEquals("E", dir);
		
		angle = 180;
		dir = SatelliteUtil.getDirectionString(angle);
		Assertions.assertEquals("S", dir);
		
		dir = "NNE";
		angle = SatelliteUtil.getDirectionDeg(dir);
		Assertions.assertEquals(23, angle);
		
		dir = "NNW";
		angle = SatelliteUtil.getDirectionDeg(dir);
		Assertions.assertEquals(360-22, angle);
		
		dir = "N";
		angle = SatelliteUtil.getDirectionDeg(dir);
		Assertions.assertEquals(0, angle);
		
		dir = "S";
		angle = SatelliteUtil.getDirectionDeg(dir);
		Assertions.assertEquals(180, angle);
		
		dir = "Fred";
		try {
			angle = SatelliteUtil.getDirectionDeg(dir);
			Assertions.fail("Error should have been thrown for invalid direction '" + dir + "'");
		} catch (IllegalArgumentException e) {
			// Expected, ignore
		}
	}
}
