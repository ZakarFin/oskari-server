package org.oskari.geojson;

import java.util.List;
import java.util.Map;

public class GeoJSONUtil {

    /*
    In GeoJSON the default geometry of a Feature is not part of the properties map
    But in SimpleFeature the default geometry is considered a regular attribute
    The unorthodox value specified here is used as the name of the attribute for the
    default geometry extracted from the GeoJSON 'geometry' field in order to
    minimize the risk of the name conflicting with an existing attribute name
     */
    public static final String DEFAULT_GEOMETRY_ATTRIBUTE_NAME = "_geometry";

    // For WGS84: 11.132mm precision at equator, more precise elsewhere, max error 5.5mm
    private static final int NUM_DECIMAL_PLACES_DEGREE = 7;
    // For metric projections: 10mm precision, max error 5mm
    private static final int NUM_DECIMAL_PLACES_OTHER = 2;

    /**
     * Get number of decimal places to use (maximum) when writing out the GeoJSON response.
     * The goal is to reduce the size of the actual response thereby reducing the amount
     * of memory and network used to serve the response while maintaining a precision that
     * still far exceedes the needs for our purposes
     *
     * @returns number of decimal places to use
     * - NUM_DECIMAL_PLACES_DEGREE for degrees
     * - NUM_DECIMAL_PLACES_OTHER for others (metres, feet, what have you)
     */
    public static int getNumDecimals(boolean isUnitDegrees) {
        return isUnitDegrees ? NUM_DECIMAL_PLACES_DEGREE : NUM_DECIMAL_PLACES_OTHER;
    }

    @SuppressWarnings("unchecked")
    public static Map<String, Object> getMap(Map<String, Object> map, String key) {
        return (Map<String, Object>) map.get(key);
    }

    @SuppressWarnings("unchecked")
    public static Map<String, Object> getMap(List<Object> list, int i) {
        return (Map<String, Object>) list.get(i);
    }

    @SuppressWarnings("unchecked")
    public static List<Object> getList(Map<String, Object> map, String key) {
        return (List<Object>) map.get(key);
    }

    @SuppressWarnings("unchecked")
    public static List<Object> getList(List<Object> list, int i) {
        return (List<Object>) list.get(i);
    }

    public static String getString(Map<String, Object> map, String key) {
        Object o = map.get(key);
        return o == null ? null : o.toString();
    }

    public static double getDouble(List<Object> list, int i) {
        return ((Number) list.get(i)).doubleValue();
    }

}
