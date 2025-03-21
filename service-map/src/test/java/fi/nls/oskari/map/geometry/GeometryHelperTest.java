package fi.nls.oskari.map.geometry;

import org.junit.jupiter.api.Assertions;
import org.locationtech.jts.geom.CoordinateSequence;
import org.locationtech.jts.geom.GeometryFactory;
import org.junit.jupiter.api.Test;

public class GeometryHelperTest {

    @Test
    public void testInterpolate() {
        GeometryFactory gf = new GeometryFactory();
        CoordinateSequence cs = gf.getCoordinateSequenceFactory().create(2, 2);
        cs.setOrdinate(0, 0, -180.0);
        cs.setOrdinate(0, 1,   45.0);
        cs.setOrdinate(1, 0,  180.0);
        cs.setOrdinate(1, 1,   45.0);
        CoordinateSequence interpolated = GeometryHelper.interpolateLinear(
                gf.createLineString(cs), 20.0, gf);

        double lon = -180.0;
        double lat =   45.0;
        for (int i = 0; i <= 18; i++) {
            Assertions.assertEquals(lon, interpolated.getOrdinate(i, 0), 0.0);
            Assertions.assertEquals(lat, interpolated.getOrdinate(i, 1), 0.0);
            lon += 20.0;
        }
    }

    @Test
    public void testIsWithin() {
        GeometryFactory gf = new GeometryFactory();
        CoordinateSequence cs = gf.getCoordinateSequenceFactory().create(2, 2);
        cs.setOrdinate(0, 0, -180.0);
        cs.setOrdinate(0, 1,  -45.0);
        cs.setOrdinate(1, 0,  180.0);
        cs.setOrdinate(1, 1,   45.0);

        Assertions.assertTrue(GeometryHelper.isWithin(cs,  -180.0, -45.0, 180.0,  45.0), "Happy");
        Assertions.assertFalse(GeometryHelper.isWithin(cs, -160.0, -45.0, 180.0,  45.0), "< minX");
        Assertions.assertFalse(GeometryHelper.isWithin(cs, -180.0, -45.0, 130.0,  45.0), "> maxX");
        Assertions.assertFalse(GeometryHelper.isWithin(cs, -180.0,  20.0, 180.0,  45.0), "< minY");
        Assertions.assertFalse(GeometryHelper.isWithin(cs, -180.0, -45.0, 180.0,   5.0), "> maxY");

        try {
            GeometryHelper.isWithin(cs, 2, 5, 1, 6);
            Assertions.fail();
        } catch (IllegalArgumentException ignore) {
            Assertions.assertEquals("maxX < minX", ignore.getMessage());
        }
        try {
            GeometryHelper.isWithin(cs, 1, -5, 2, -7);
            Assertions.fail();
        } catch (IllegalArgumentException ignore) {
            Assertions.assertEquals("maxY < minY", ignore.getMessage());
        }
    }
}
