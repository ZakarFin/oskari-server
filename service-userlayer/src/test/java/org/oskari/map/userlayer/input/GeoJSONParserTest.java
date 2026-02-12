package org.oskari.map.userlayer.input;

import java.io.File;
import java.net.URISyntaxException;

import org.geotools.api.feature.simple.SimpleFeature;
import org.geotools.api.referencing.FactoryException;
import org.geotools.api.referencing.NoSuchAuthorityCodeException;
import org.geotools.api.referencing.crs.CoordinateReferenceSystem;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.referencing.CRS;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateSequence;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Polygon;

import fi.nls.oskari.service.ServiceException;

public class GeoJSONParserTest {

    @Test
    public void testParse() throws ServiceException, URISyntaxException, NoSuchAuthorityCodeException, FactoryException {
        File file = new File(GeoJSONParserTest.class.getResource("myplaces_export.json").toURI());
        CoordinateReferenceSystem sourceCRS = null; // Should auto detect based on Named CRS object
        CoordinateReferenceSystem targetCRS = CRS.decode("EPSG:3857");

        GeoJSONParser parser = new GeoJSONParser();
        SimpleFeatureCollection fc = parser.parse(file, sourceCRS, targetCRS);

        Assertions.assertEquals(1, fc.size(), "There is 1 Feature in the file");

        CoordinateReferenceSystem crs = fc.getSchema().getGeometryDescriptor().getCoordinateReferenceSystem();
        Assertions.assertEquals(targetCRS, crs);

        SimpleFeature f = fc.features().next();
        Geometry geom = (Geometry) f.getDefaultGeometry();
        Assertions.assertTrue(geom instanceof MultiPolygon);
        MultiPolygon mp = (MultiPolygon) geom;
        Assertions.assertEquals(1, mp.getNumGeometries());
        Polygon p = (Polygon) mp.getGeometryN(0);
        CoordinateSequence exterior = p.getExteriorRing().getCoordinateSequence();
        Coordinate first = exterior.getCoordinate(0);
        Assertions.assertEquals(2220954.29385408, first.x, 1e6);
        Assertions.assertEquals(1.111190502475099E7, first.y, 1e6);
        Assertions.assertTrue(Double.isNaN(first.z));
    }

}
