
package fi.nls.oskari.routing.pojo;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.HashMap;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class LegGeometry {

    @JsonProperty("points")
    private String points;
    @JsonProperty("length")
    private Long length;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /**
     * 
     * @return
     *     The points
     */
    @JsonProperty("points")
    public String getPoints() {
        return points;
    }

    /**
     * 
     * @param points
     *     The points
     */
    @JsonProperty("points")
    public void setPoints(String points) {
        this.points = points;
    }

    /**
     * 
     * @return
     *     The length
     */
    @JsonProperty("length")
    public Long getLength() {
        return length;
    }

    /**
     * 
     * @param length
     *     The length
     */
    @JsonProperty("length")
    public void setLength(Long length) {
        this.length = length;
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

}
