package com.diozero.satellite;

import org.junit.Assert;
import org.junit.Test;

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
		Assert.assertEquals("N", dir);
		
		angle = 11;
		dir = SatelliteUtil.getDirectionString(angle);
		Assert.assertEquals("N", dir);
		
		angle = 360-11;
		dir = SatelliteUtil.getDirectionString(angle);
		Assert.assertEquals("N", dir);
		
		angle = 12;
		dir = SatelliteUtil.getDirectionString(angle);
		Assert.assertEquals("NNE", dir);
		
		angle = 90;
		dir = SatelliteUtil.getDirectionString(angle);
		Assert.assertEquals("E", dir);
		
		angle = 180;
		dir = SatelliteUtil.getDirectionString(angle);
		Assert.assertEquals("S", dir);
		
		dir = "NNE";
		angle = SatelliteUtil.getDirectionDeg(dir);
		Assert.assertEquals(23, angle);
		
		dir = "NNW";
		angle = SatelliteUtil.getDirectionDeg(dir);
		Assert.assertEquals(360-22, angle);
		
		dir = "N";
		angle = SatelliteUtil.getDirectionDeg(dir);
		Assert.assertEquals(0, angle);
		
		dir = "S";
		angle = SatelliteUtil.getDirectionDeg(dir);
		Assert.assertEquals(180, angle);
		
		dir = "Fred";
		try {
			angle = SatelliteUtil.getDirectionDeg(dir);
			Assert.fail("Error should have been thrown for invalid direction '" + dir + "'");
		} catch (IllegalArgumentException e) {
			// Expected, ignore
		}
	}
}
