package org.oskari.control.myfeatures.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import fi.nls.oskari.domain.map.myfeatures.MyFeaturesFieldInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CreateMyFeaturesLayer {

    private List<MyFeaturesFieldInfo> layerFields;
    private Map<String, Map<String, Object>> locale;
    private Map<String, Object> style;

    public List<MyFeaturesFieldInfo> getLayerFields() {
        return layerFields;
    }

    public void setLayerFields(List<MyFeaturesFieldInfo> layerFields) {
        this.layerFields = layerFields;
    }

    public Map<String, Map<String, Object>> getLocale() {
        return locale;
    }

    public void setLocale(Map<String, Map<String, Object>> locale) {
        this.locale = locale;
    }

    public Map<String, Object> getStyle() {
        return style;
    }

    public void setStyle(Map<String, Object> style) {
        this.style = style;
    }

    public List<String> validate() {
        List<String> errors = new ArrayList<>();

        if (layerFields == null || layerFields.isEmpty()) {
            errors.add("layerFields must be non-null and non-empty");
        }

        if (locale == null || locale.isEmpty()) {
            errors.add("locale must be non-null and non-empty");
        }

        return errors;
    }

}
