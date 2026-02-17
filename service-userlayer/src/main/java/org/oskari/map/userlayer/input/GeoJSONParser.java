package org.oskari.map.userlayer.input;

import java.io.File;

import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.referencing.CRS;
import org.geotools.api.referencing.crs.CoordinateReferenceSystem;
import org.oskari.map.userlayer.service.UserLayerException;
import org.oskari.maplayer.GeoJSONStringReader;

import fi.nls.oskari.service.ServiceException;

public class GeoJSONParser implements FeatureCollectionParser {

    public static final String SUFFIX = "GEOJSON";

    @Override
    public String getSuffix() {
        return SUFFIX;
    }

    @Override
    public SimpleFeatureCollection parse(File file, CoordinateReferenceSystem sourceCRS,
            CoordinateReferenceSystem targetCRS) throws ServiceException {
        try {
            SimpleFeatureCollection src = GeoJSONStringReader.readGeoJSON(file, sourceCRS);
            sourceCRS = src.getSchema().getCoordinateReferenceSystem();
            return CRS.equalsIgnoreMetadata(sourceCRS, targetCRS)
                ? src
                : FeatureCollectionParsers.read(() -> src, sourceCRS, targetCRS);
        } catch (ServiceException e) {
            // forward error on read: if in file UserLayerException. if in service ServiceException
            throw e;
        } catch (Exception e) {
            throw new UserLayerException("Failed to parse GeoJSON: " + e.getMessage(),
                    UserLayerException.ErrorType.PARSER, UserLayerException.ErrorType.INVALID_FORMAT);
        }
    }

}
