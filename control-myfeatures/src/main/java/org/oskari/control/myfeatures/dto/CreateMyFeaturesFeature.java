package org.oskari.control.myfeatures.dto;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.locationtech.jts.geom.Geometry;

import com.fasterxml.jackson.databind.ObjectMapper;

import fi.nls.oskari.control.ActionParamsException;
import fi.nls.oskari.domain.map.myfeatures.MyFeaturesFeature;
import fi.nls.oskari.domain.map.myfeatures.MyFeaturesLayer;
import fi.nls.oskari.util.JSONHelper;

public class CreateMyFeaturesFeature {

    private String layerId;
    private Geometry geometry;
    private Map<String, Object> properties;

    public String getLayerId() {
        return layerId;
    }

    public void setLayerId(String layerId) {
        this.layerId = layerId;
    }

    public Geometry getGeometry() {
        return geometry;
    }

    public void setGeometry(Geometry geometry) {
        this.geometry = geometry;
    }

    public Map<String, Object> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, Object> properties) {
        this.properties = properties;
    }

    public List<String> validate() {
        List<String> errors = new ArrayList<>();
        if (layerId == null) {
            errors.add("layerId is required");
        } else if (MyFeaturesLayer.parseLayerId(layerId).isEmpty()) {
            errors.add("layerId format");
        }
        if (geometry == null) {
            errors.add("geometry is required");
        }
        return errors;
    }

    public MyFeaturesFeature toDomain(ObjectMapper om) throws ActionParamsException {
        try {
            MyFeaturesFeature feature = new MyFeaturesFeature();
            feature.setGeometry(geometry);
            if (properties != null) {
                feature.setProperties(JSONHelper.createJSONObject(om.writeValueAsString(properties)));
            }
            return feature;
        } catch (Exception e) {
            throw new ActionParamsException("Failed to convert to domain model", e);
        }
    }

}
