package org.oskari.maplayer;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.referencing.CRS;
import org.geotools.api.feature.simple.SimpleFeatureType;
import org.geotools.api.referencing.crs.CoordinateReferenceSystem;
import org.oskari.geojson.GeoJSONReader2;
import org.oskari.geojson.GeoJSONSchemaDetector;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class GeoJSONStringReader {

    private static final ObjectMapper OM = new ObjectMapper();
    private static final TypeReference<HashMap<String, Object>> TYPE_REF = new TypeReference<HashMap<String, Object>>() {};

    public static SimpleFeatureCollection readGeoJSON(InputStream in, CoordinateReferenceSystem crs) throws Exception {
        return convert(loadJSONResource(in), crs);
    }

    public static SimpleFeatureCollection readGeoJSON(String geojson, String srs) throws Exception{
        return readGeoJSON(geojson, CRS.decode(srs));
    }

    public static SimpleFeatureCollection readGeoJSON(String geojson, CoordinateReferenceSystem crs) throws Exception {
        return convert(loadJSONResource(geojson), crs);
    }

    public static SimpleFeatureCollection readGeoJSON(File file, CoordinateReferenceSystem crs) throws Exception {
        return convert(loadJSONResource(file), crs);
    }

    private static SimpleFeatureCollection convert(Map<String, Object> geojsonAsMap, CoordinateReferenceSystem crs) throws Exception {
        if (crs == null) {
            crs = GeoJSONSchemaDetector.detectCrs(geojsonAsMap);
        }
        boolean ignoreGeometryProperties = true;
        try {
            SimpleFeatureType schema = GeoJSONSchemaDetector.getSchema(geojsonAsMap, crs, ignoreGeometryProperties);
            return GeoJSONReader2.toFeatureCollection(geojsonAsMap, schema);
        } catch (NullPointerException e) {
            throw new IllegalArgumentException("Input was not GeoJSON");
        }
    }

    private static Map<String, Object> loadJSONResource(InputStream in) throws Exception {
        try {
            return OM.readValue(in, TYPE_REF);
        } catch (MismatchedInputException e) {
            throw new IllegalArgumentException("Input couldn't be parsed as JSON Object");
        }
    }

    private static Map<String, Object> loadJSONResource(String geojson) throws Exception {
        try {
            return OM.readValue(geojson, TYPE_REF);
        } catch (MismatchedInputException e) {
            throw new IllegalArgumentException("Input couldn't be parsed as JSON Object");
        }
    }

    private static Map<String, Object> loadJSONResource(File file) throws Exception {
        try {
            return OM.readValue(file, TYPE_REF);
        } catch (MismatchedInputException e) {
            throw new IllegalArgumentException("Input couldn't be parsed as JSON Object");
        }
    }

}
