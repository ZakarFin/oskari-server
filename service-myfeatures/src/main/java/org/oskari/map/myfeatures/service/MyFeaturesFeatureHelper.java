package org.oskari.map.myfeatures.service;

import fi.nls.oskari.domain.map.myfeatures.MyFeaturesFeature;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.map.geometry.WKTHelper;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.locationtech.jts.geom.Geometry;
import org.oskari.geojson.GeoJSON;
import org.oskari.geojson.GeoJSONWriter;

import java.util.List;
import java.util.stream.Collectors;

import static fi.nls.oskari.map.geometry.WKTHelper.parseWKT;

public class MyFeaturesFeatureHelper {
    private static final GeoJSONWriter geojsonWriter = new GeoJSONWriter();
    private static final Logger LOG = LogFactory.getLogger(MyFeaturesFeatureHelper.class);
    public static JSONObject toGeoJSONFeatureCollection(List<MyFeaturesFeature> featuresList, String targetSRSName) {
        if (featuresList == null || featuresList.isEmpty()) {
            return null;
        }
        JSONObject json = new JSONObject();
        try {
            json.put(GeoJSON.TYPE, GeoJSON.FEATURE_COLLECTION);
            json.put("crs", geojsonWriter.writeCRSObject(targetSRSName));

            JSONArray features = new JSONArray(featuresList.stream().map(feature -> toGeoJSONFeature(feature, targetSRSName)).collect(Collectors.toList()));
            json.put(GeoJSON.FEATURES, features);

        } catch(JSONException ex) {
            LOG.warn("Failed to create GeoJSON FeatureCollection");
        }
        return json;
    }

    private static JSONObject toGeoJSONFeature(MyFeaturesFeature feature, String targetSRSName) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("id", feature.getId());
            jsonObject.put("geometry_name", GeoJSON.GEOMETRY);
            jsonObject.put(GeoJSON.TYPE, GeoJSON.FEATURE);

            String sourceSRSName = "EPSG:" + feature.getDatabaseSRID();
            Geometry transformed = WKTHelper.transform(parseWKT(feature.getGeometry().toString()), sourceSRSName, targetSRSName);
            JSONObject geoJsonGeometry = geojsonWriter.writeGeometry(transformed);
            jsonObject.put(GeoJSON.GEOMETRY, geoJsonGeometry);
            jsonObject.put("properties", feature.getProperties());

        } catch(JSONException ex) {
            LOG.warn("Failed to convert MyFeaturesFeature to GeoJSONFeature");
        }

        return jsonObject;
    }

}
