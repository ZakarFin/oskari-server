package org.oskari.map.userlayer.input;

public class JSONParser extends GeoJSONParser {

    public static final String SUFFIX = "JSON";

    @Override
    public String getSuffix() {
        return SUFFIX;
    }

}
