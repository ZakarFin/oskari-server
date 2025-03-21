package fi.nls.oskari.routing.pojo;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Place {
    @JsonProperty("name")
    private String name;
    @JsonProperty("lon")
    private Double lon;
    @JsonProperty("lat")
    private Double lat;
    @JsonProperty("orig")
    private String orig;
    @JsonProperty("vertexType")
    private String vertexType;

    @JsonProperty("arrival")
    private ScheduledTime arrival;

    @JsonProperty("departure")
    private ScheduledTime departure;

    @JsonProperty("stop")
    private Stop stop;

    @JsonProperty("stopPosition")
    private StopPosition stopPosition;
    /**
     *
     * @return
     *     The name
     */
    @JsonProperty("name")
    public String getName() {
        return name;
    }

    /**
     *
     * @param name
     *     The name
     */
    @JsonProperty("name")
    public void setName(String name) {
        this.name = name;
    }

    /**
     *
     * @return
     *     The lon
     */
    @JsonProperty("lon")
    public Double getLon() {
        return lon;
    }

    /**
     *
     * @param lon
     *     The lon
     */
    @JsonProperty("lon")
    public void setLon(Double lon) {
        this.lon = lon;
    }

    /**
     *
     * @return
     *     The lat
     */
    @JsonProperty("lat")
    public Double getLat() {
        return lat;
    }

    /**
     *
     * @param lat
     *     The lat
     */
    @JsonProperty("lat")
    public void setLat(Double lat) {
        this.lat = lat;
    }

    /**
     *
     * @return
     *     The orig
     */
    @JsonProperty("orig")
    public String getOrig() {
        return orig;
    }

    /**
     *
     * @param orig
     *     The orig
     */
    @JsonProperty("orig")
    public void setOrig(String orig) {
        this.orig = orig;
    }

    /**
     *
     * @return
     *     The vertexType
     */
    @JsonProperty("vertexType")
    public String getVertexType() {
        return vertexType;
    }

    /**
     *
     * @param vertexType
     *     The vertexType
     */
    @JsonProperty("vertexType")
    public void setVertexType(String vertexType) {
        this.vertexType = vertexType;
    }

    public ScheduledTime getArrival() {
        return arrival;
    }

    public void setArrival(ScheduledTime arrival) {
        this.arrival = arrival;
    }

    public ScheduledTime getDeparture() {
        return departure;
    }

    public void setDeparture(ScheduledTime departure) {
        this.departure = departure;
    }

    public Stop getStop() {
        return stop;
    }

    public void setStop(Stop stop) {
        this.stop = stop;
    }

    public StopPosition getStopPosition() {
        return stopPosition;
    }

    public void setStopPosition(StopPosition stopPosition) {
        this.stopPosition = stopPosition;
    }
}
