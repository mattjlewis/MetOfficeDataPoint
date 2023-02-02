package com.diozero.satellite.orekit;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import org.hipparchus.ode.events.Action;
import org.hipparchus.util.FastMath;
import org.orekit.errors.OrekitException;
import org.orekit.frames.TopocentricFrame;
import org.orekit.propagation.SpacecraftState;
import org.orekit.propagation.analytical.tle.TLE;
import org.orekit.propagation.analytical.tle.TLEPropagator;
import org.orekit.propagation.events.ElevationDetector;
import org.orekit.propagation.events.ElevationExtremumDetector;
import org.orekit.propagation.events.handlers.EventHandler;
import org.orekit.time.AbsoluteDate;
import org.orekit.time.TimeScalesFactory;

import com.diozero.location.GeographicLocation;
import com.diozero.satellite.SatelliteFlyby;

public class SatelliteUtil {
	public static List<SatelliteFlyby> calculateVisibleFlybys(TLE tle, GeographicLocation obsLoc,
			double minElevationDeg, double minPeakElevationDeg, AbsoluteDate startDate, AbsoluteDate endDate)
			throws OrekitException {
		OrekitUtil.initialise();

		return new FlybyCalculator().calculate(tle, OrekitUtil.getEarthTopcentricFrame(obsLoc), minElevationDeg,
				minPeakElevationDeg, startDate, endDate);
	}

	private static final class FlybyCalculator {
		// Input values
		TopocentricFrame frame;

		// Calculated values
		AbsoluteDate approachDate;
		int approachElevationDeg;
		int approachAzimuthDeg;
		AbsoluteDate peakDate;
		int peakElevationDeg;
		int peakAzimuthDeg;
		List<SatelliteFlyby> flybys;

		public FlybyCalculator() {
		}

		public List<SatelliteFlyby> calculate(TLE tle, TopocentricFrame frame, double minElevationDeg,
				double minPeakElevationDeg, AbsoluteDate startDate, AbsoluteDate endDate) throws OrekitException {
			this.frame = frame;
			flybys = new ArrayList<>();

			TLEPropagator propagator = TLEPropagator.selectExtrapolator(tle);
			// Triggered at raising or setting time of a satellite with respect to a ground
			// point,
			// taking atmospheric refraction into account and either constant elevation or
			// ground
			// mask when threshold elevation is azimuth-dependent
			double max_check_interval_sec = 60;
			double max_divergence_threshold_sec = 0.001;
			VisibilityHandler visibility_handler = new VisibilityHandler(minPeakElevationDeg);
			ElevationDetector elev_detector = new ElevationDetector(max_check_interval_sec,
					max_divergence_threshold_sec, frame).withConstantElevation(FastMath.toRadians(minElevationDeg))
							.withHandler(visibility_handler);
			propagator.addEventDetector(elev_detector);
			// Triggered at maximum (or minimum) satellite elevation with respect to a
			// ground point
			ElevationExtremumHandler elevation_extremum_handler = new ElevationExtremumHandler();
			ElevationExtremumDetector elev_extremum_detector = new ElevationExtremumDetector(frame)
					.withHandler(elevation_extremum_handler);
			propagator.addEventDetector(elev_extremum_detector);

			/* SpacecraftState final_state = */propagator.propagate(startDate, endDate);

			return flybys;
		}

		private class VisibilityHandler implements EventHandler<ElevationDetector> {
			private double minPeakElevationDeg;

			public VisibilityHandler(double minPeakElevationDeg) {
				this.minPeakElevationDeg = minPeakElevationDeg;
			}

			@Override
			public Action eventOccurred(SpacecraftState s, ElevationDetector detector, boolean increasing)
					throws OrekitException {

				if (increasing) {
					approachDate = s.getDate();
					approachElevationDeg = (int) Math.round(FastMath.toDegrees(
							frame.getElevation(s.getPVCoordinates().getPosition(), s.getFrame(), s.getDate())));
					approachAzimuthDeg = (int) Math.round(FastMath.toDegrees(
							frame.getAzimuth(s.getPVCoordinates().getPosition(), s.getFrame(), s.getDate())));

					return Action.CONTINUE;
				}

				if (approachDate != null) {
					ZonedDateTime approach_date_time = ZonedDateTime.ofInstant(
							approachDate.toDate(TimeScalesFactory.getUTC()).toInstant(), ZoneId.systemDefault());

					ZonedDateTime peak_date_time = null;
					if (peakDate != null) {
						peak_date_time = ZonedDateTime.ofInstant(
								peakDate.toDate(TimeScalesFactory.getUTC()).toInstant(), ZoneId.systemDefault());
					}

					if (peakElevationDeg > minPeakElevationDeg) {
						ZonedDateTime departure_date_time = ZonedDateTime.ofInstant(
								s.getDate().toDate(TimeScalesFactory.getUTC()).toInstant(), ZoneId.systemDefault());
						int departure_elevation_deg = (int) Math.round(FastMath.toDegrees(
								frame.getElevation(s.getPVCoordinates().getPosition(), s.getFrame(), s.getDate())));
						int departure_azimuth_deg = (int) Math.round(FastMath.toDegrees(
								frame.getAzimuth(s.getPVCoordinates().getPosition(), s.getFrame(), s.getDate())));

						flybys.add(new SatelliteFlyby(approach_date_time, approachElevationDeg, approachAzimuthDeg,
								peak_date_time, peakElevationDeg, peakAzimuthDeg, departure_date_time,
								departure_elevation_deg, departure_azimuth_deg));
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

		final class ElevationExtremumHandler implements EventHandler<ElevationExtremumDetector> {
			@Override
			public Action eventOccurred(SpacecraftState s, ElevationExtremumDetector detector, boolean increasing)
					throws OrekitException {
				peakDate = s.getDate();
				peakElevationDeg = (int) Math.round(FastMath
						.toDegrees(frame.getElevation(s.getPVCoordinates().getPosition(), s.getFrame(), s.getDate())));
				peakAzimuthDeg = (int) Math.round(FastMath
						.toDegrees(frame.getAzimuth(s.getPVCoordinates().getPosition(), s.getFrame(), s.getDate())));

				return Action.CONTINUE;
			}
		}
	}
}
