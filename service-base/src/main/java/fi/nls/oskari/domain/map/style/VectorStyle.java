package fi.nls.oskari.domain.map.style;


import java.io.Serializable;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonIgnore;

import com.fasterxml.jackson.annotation.JsonSetter;
import fi.nls.oskari.util.JSONHelper;
import org.json.JSONObject;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class VectorStyle implements Serializable {
    public static final String TYPE_OSKARI = "oskari";
    public static final String TYPE_MAPBOX = "mapbox";
    public static final String TYPE_3D = "cesium";
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_INSTANT;

    private long id;
    private Integer layerId; // null for default instance style
    private String type;
    private Long creator; // null for admin created styles
    private String name;
    private JSONObject style;
    private OffsetDateTime created;
    private OffsetDateTime updated;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Integer getLayerId() {
        return layerId;
    }

    public void setLayerId(Integer layerId) {
        this.layerId = layerId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @JsonIgnore
    public Long getCreator() {
        return creator;
    }

    public void setCreator(Long creator) {
        this.creator = creator;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public JSONObject getStyle() {
        return style;
    }

    @JsonGetter("style")
    public Map getStyleMap() {
        return JSONHelper.getObjectAsMap(style);
    }

    public void setStyle(JSONObject style) {
        this.style = style;
    }

    @JsonSetter("style")
    public void setStyleMap(Map<String, Object> style) {
        this.style = new JSONObject(style);
    }

    @JsonGetter("created")
    public String getFormattedCreated () {
        return created != null ? created.format(FORMATTER) : null;
    }
    public OffsetDateTime getCreated() {
        return created;
    }

    @JsonIgnore
    public void setCreated(OffsetDateTime created) {
        this.created = created;
    }

    @JsonGetter("updated")
    public String getFormattedUpdated () {
        return updated != null ? updated.format(FORMATTER) : null;
    }
    public OffsetDateTime getUpdated() {
        return updated;
    }

    @JsonIgnore
    public void setUpdated(OffsetDateTime updated) {
        this.updated = updated;
    }
}
