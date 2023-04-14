package org.oskari.capabilities;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.oskari.capabilities.ogc.LayerStyle;

import java.util.*;

// Something that can be serialized to oskari_maplayer.capabilities
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class LayerCapabilities {

    private String name;
    private String title;
    private String type;
    private List<LayerStyle> styles;
    private Set<String> srs;
    private String defaultStyle;
    private String url;

    public LayerCapabilities(@JsonProperty("name") String name, @JsonProperty("title") String title) {
        this.name = name;
        this.title = title;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setStyles(List<LayerStyle> styles) {
        setStyles(styles, null);
    }
    public void setStyles(List<LayerStyle> styles, String defaultStyle) {
        this.styles = styles;
        this.defaultStyle = defaultStyle;
        if (defaultStyle == null && !styles.isEmpty()) {
            this.defaultStyle = styles.get(0).getName();
        }
    }
    public void setSrs(Set<String> supported) {
        this.srs = supported;
    }

    public String getName() {
        return name;
    }

    public String getTitle() {
        return title;
    }

    public List<LayerStyle> getStyles() {
        if (styles == null) {
            return Collections.emptyList();
        }
        return styles;
    }

    public Set<String> getSrs() {
        if (srs == null) {
            return Collections.emptySet();
        }
        return srs;
    }

    public String getDefaultStyle() {
        if (defaultStyle == null && styles != null && !styles.isEmpty()) {
            return styles.get(0).getName();
        }
        return defaultStyle;
    }

}
