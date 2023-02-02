package com.diozero.satellite.orekit;

import org.hipparchus.util.FastMath;
import org.orekit.bodies.BodyShape;
import org.orekit.bodies.GeodeticPoint;
import org.orekit.bodies.OneAxisEllipsoid;
import org.orekit.data.DataContext;
import org.orekit.data.DataProvidersManager;
import org.orekit.errors.OrekitException;
import org.orekit.frames.Frame;
import org.orekit.frames.FramesFactory;
import org.orekit.frames.TopocentricFrame;
import org.orekit.utils.Constants;
import org.orekit.utils.IERSConventions;

import com.diozero.location.GeographicLocation;

public class OrekitUtil {
	private static boolean initialised = false;

	public static void initialise() throws OrekitException {
		synchronized (OrekitUtil.class) {
			if (!initialised) {
				if (System.getProperty(DataProvidersManager.OREKIT_DATA_PATH) == null) {
					System.setProperty(DataProvidersManager.OREKIT_DATA_PATH, "src/main/resources/orekit-data");
				}
				DataContext.getDefault().getDataProvidersManager().addDefaultProviders();

				initialised = true;
			}
		}
	}

	public static TopocentricFrame getEarthTopcentricFrame(GeographicLocation observationLocation)
			throws OrekitException {
		initialise();

		double latitude = FastMath.toRadians(observationLocation.getLatitude());
		double longitude = FastMath.toRadians(observationLocation.getLongitude());
		GeodeticPoint point = new GeodeticPoint(latitude, longitude, observationLocation.getAltitude());

		Frame earth_frame = FramesFactory.getITRF(IERSConventions.IERS_2010, true);
		BodyShape earth_bs = new OneAxisEllipsoid(Constants.WGS84_EARTH_EQUATORIAL_RADIUS,
				Constants.WGS84_EARTH_FLATTENING, earth_frame);

		return new TopocentricFrame(earth_bs, point,
				String.join(",", observationLocation.getCityName(), observationLocation.getCountryCode()));
	}
}
