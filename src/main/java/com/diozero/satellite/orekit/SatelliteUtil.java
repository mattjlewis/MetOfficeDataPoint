package com.diozero.satellite.orekit;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.hipparchus.util.FastMath;
import org.hipparchus.util.MathUtils;
import org.orekit.errors.OrekitException;
import org.orekit.frames.TopocentricFrame;
import org.orekit.propagation.SpacecraftState;
import org.orekit.propagation.analytical.tle.TLE;
import org.orekit.propagation.analytical.tle.TLEPropagator;
import org.orekit.propagation.events.ElevationDetector;
import org.orekit.propagation.events.ElevationExtremumDetector;
import org.orekit.propagation.events.handlers.EventHandler;
import org.orekit.time.AbsoluteDate;
import org.orekit.time.TimeScale;
import org.orekit.time.TimeScalesFactory;

import com.diozero.satellite.ObservationLocation;
import com.diozero.satellite.SatelliteFlyby;

public class SatelliteUtil {
	private static final String[] ANGLES = {
			"N", "NNE", "NE", "ENE", "E", "ESE", "SE", "SSE", "S", "SSW", "SW", "WSW", "W", "WNW", "NW", "NNW" };
	
	public static List<SatelliteFlyby> calculateVisibleFlybys(TLE tle, ObservationLocation obsLoc, double minElevationDeg,
			double minPeakElevationDeg, Date startDate, Date endDate) throws OrekitException {
		OrekitUtil.initialise();
		
		TimeScale utc = TimeScalesFactory.getUTC();
		
		return calculateVisibleFlybys(tle, obsLoc, minElevationDeg, minPeakElevationDeg,
				new AbsoluteDate(startDate, utc), new AbsoluteDate(endDate, utc));
	}
	
	public static List<SatelliteFlyby> calculateVisibleFlybys(TLE tle, ObservationLocation obsLoc, double minElevationDeg,
			double minPeakElevationDeg, AbsoluteDate startDate, AbsoluteDate endDate) throws OrekitException {
		OrekitUtil.initialise();
		
		TopocentricFrame topocentric_frame = OrekitUtil.getEarthTopcentricFrame(obsLoc);
				
		FlybyCalculator calculator = new FlybyCalculator();
		return calculator.calculate(tle, topocentric_frame, minElevationDeg, minPeakElevationDeg, startDate, endDate);
	}
	
	static void printState(TopocentricFrame frame, SpacecraftState s) throws OrekitException {
		System.out.println("Position: " + s.getPVCoordinates().getPosition());
		System.out.println("Frame Position: " + s.getPVCoordinates(frame).getPosition());
		double azimuth = frame.getAzimuth(s.getPVCoordinates().getPosition(), s.getFrame(), s.getDate());
		double elevation = frame.getElevation(s.getPVCoordinates().getPosition(), s.getFrame(), s.getDate());
		System.out.println("Semi-major Axis: " + s.getA() + ", Eccentricity: " + s.getE()
			+ ", Inclination: " + FastMath.toDegrees(s.getI())
			+ ", Mean Latitude: " + FastMath.toDegrees(MathUtils.normalizeAngle(s.getLM(), FastMath.PI)) 
			+ ", Azimuth=" + FastMath.toDegrees(azimuth) + ", Elevation=" + FastMath.toDegrees(elevation));
	}

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
	 * @param angle
	 * @return
	 */
	public static String getDirectionString(int angleDeg) {
		return ANGLES[((int) Math.round(angleDeg / (360d / 16))) % 16];
	}

	public static int getDirectionDeg(String compassDir) {
		int i;
		for (i=0; i<ANGLES.length && (!ANGLES[i].equals(compassDir)); i++) {
		}
		
		if (i >= 16) {
			throw new IllegalArgumentException("Invalid compass direction '" + compassDir + "'");
		}
		
		return (int) (Math.round(i * (360d / 16)));
	}
}

class FlybyCalculator {
	// Input values
	private TopocentricFrame frame;
	
	// Calculated values
	private AbsoluteDate approachDate;
	private int approachElevationDeg;
	private int approachAzimuthDeg;
	private AbsoluteDate peakDate;
	private int peakElevationDeg;
	private int peakAzimuthDeg;
	private List<SatelliteFlyby> flybys;
	
	public FlybyCalculator() {
	}
	
	public List<SatelliteFlyby> calculate(TLE tle, TopocentricFrame frame, double minElevationDeg,
			double minPeakElevationDeg, AbsoluteDate startDate, AbsoluteDate endDate) throws OrekitException {
		this.frame = frame;
		flybys = new ArrayList<>();
		
		//System.out.println("Propagating for TLE: " + tle.getSatelliteNumber() + ", " + tle.getLaunchNumber() + ", " + tle.getLaunchYear() + ", " + tle.getDate());
		TLEPropagator propagator = TLEPropagator.selectExtrapolator(tle);
		// Triggered at raising or setting time of a satellite with respect to a ground point,
		// taking atmospheric refraction into account and either constant elevation or ground
		// mask when threshold elevation is azimuth-dependent
		double max_check_interval_sec = 60;
		double max_divergence_threshold_sec = 0.001;
		VisibilityHandler visibility_handler = new VisibilityHandler(minPeakElevationDeg);
		ElevationDetector elev_detector = new ElevationDetector(max_check_interval_sec,
				max_divergence_threshold_sec, frame)
				.withConstantElevation(FastMath.toRadians(minElevationDeg))
				.withHandler(visibility_handler);
		propagator.addEventDetector(elev_detector);
		// Triggered at maximum (or minimum) satellite elevation with respect to a ground point
		ElevationExtremumHandler elevation_extremum_handler = new ElevationExtremumHandler();
		ElevationExtremumDetector elev_extremum_detector = new ElevationExtremumDetector(frame).withHandler(elevation_extremum_handler);
		propagator.addEventDetector(elev_extremum_detector);
		
		/*SpacecraftState final_state = */propagator.propagate(startDate, endDate);
		//System.out.println("Final state: " + final_state);
		
		return flybys;
	}

	private class VisibilityHandler implements EventHandler<ElevationDetector> {
		private double minPeakElevationDeg;
		
		public VisibilityHandler(double minPeakElevationDeg) {
			this.minPeakElevationDeg = minPeakElevationDeg;
		}
		
		@Override
		public EventHandler.Action eventOccurred(SpacecraftState s,
				ElevationDetector detector, boolean increasing) throws OrekitException {
			
			if (increasing) {
				//System.out.println("START Visibility at " + detector.getTopocentricFrame().getName() + " begins at " + s.getDate());
				//Satellite.printState(frame, s);
				
				approachDate = s.getDate();
				approachElevationDeg = (int) Math.round(FastMath.toDegrees(frame.getElevation(
						s.getPVCoordinates().getPosition(), s.getFrame(), s.getDate())));
				approachAzimuthDeg = (int) Math.round(FastMath.toDegrees(frame.getAzimuth(
						s.getPVCoordinates().getPosition(), s.getFrame(), s.getDate())));
				
				return Action.CONTINUE;
			}

			//System.out.println("END Visibility at " + detector.getTopocentricFrame().getName() + " ends at " + s.getDate());
			//Satellite.printState(frame, s);
			
			if (approachDate != null) {
				ZonedDateTime approach_date_time = ZonedDateTime.ofInstant(approachDate.toDate(TimeScalesFactory.getUTC()).toInstant(), ZoneId.systemDefault());
				
				ZonedDateTime peak_date_time = null;
				if (peakDate != null) {
					peak_date_time = ZonedDateTime.ofInstant(peakDate.toDate(TimeScalesFactory.getUTC()).toInstant(), ZoneId.systemDefault());
				}
				
				if (peakElevationDeg > minPeakElevationDeg) {
					ZonedDateTime departure_date_time = ZonedDateTime.ofInstant(s.getDate().toDate(TimeScalesFactory.getUTC()).toInstant(), ZoneId.systemDefault());
					int departure_elevation_deg = (int) Math.round(FastMath.toDegrees(frame.getElevation(
							s.getPVCoordinates().getPosition(), s.getFrame(), s.getDate())));
					int departure_azimuth_deg = (int) Math.round(FastMath.toDegrees(frame.getAzimuth(
							s.getPVCoordinates().getPosition(), s.getFrame(), s.getDate())));
					
					flybys.add(new SatelliteFlyby(
							approach_date_time, approachElevationDeg, approachAzimuthDeg,
							peak_date_time, peakElevationDeg, peakAzimuthDeg,
							departure_date_time, departure_elevation_deg, departure_azimuth_deg));
				}
				
				approachDate = null;
				approachElevationDeg = 0;
				approachAzimuthDeg = 0;
				peakDate = null;
				peakElevationDeg = 0;
				peakAzimuthDeg = 0;
			}
			
			return Action.CONTINUE;
		}
	}
	
	private class ElevationExtremumHandler implements EventHandler<ElevationExtremumDetector> {
		@Override
		public EventHandler.Action eventOccurred(SpacecraftState s,
				ElevationExtremumDetector detector, boolean increasing) throws OrekitException {
			//System.out.println("ElevationExtremum event @ " + s.getDate());
			//Satellite.printState(frame, s);
			
			peakDate = s.getDate();
			peakElevationDeg = (int) Math.round(FastMath.toDegrees(frame.getElevation(
					s.getPVCoordinates().getPosition(), s.getFrame(), s.getDate())));
			peakAzimuthDeg = (int) Math.round(FastMath.toDegrees(frame.getAzimuth(
					s.getPVCoordinates().getPosition(), s.getFrame(), s.getDate())));
			
			return Action.CONTINUE;
		}
	}
}
