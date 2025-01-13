package org.oskari.capabilities;

import fi.nls.oskari.cache.Cache;
import fi.nls.oskari.cache.CacheManager;
import fi.nls.oskari.service.OskariComponent;
import fi.nls.oskari.service.ServiceException;
import fi.nls.oskari.service.ServiceUnauthorizedException;
import fi.nls.oskari.util.IOHelper;
import fi.nls.oskari.util.PropertyUtil;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.Map;

public abstract class CapabilitiesParser extends OskariComponent {

    // timeout capabilities request after 30 seconds (configurable)
    private static final int TIMEOUT_MS = PropertyUtil.getOptional("capabilities.timeout", 30) * 1000;
    private static final Cache<RawCapabilitiesResponse> XML_CACHE = CacheManager.getCache(CapabilitiesParser.class.getName());
    static {
        // 10minutes
        XML_CACHE.setExpiration(10L * 60L * 1000L);
        // we don't need to have a large cache since the layers from same domain _should_ be queried sequentially/in a row.
        XML_CACHE.setLimit(10);
    }

    /**
     * Returns all layer capabilities for all layers found on the service.
     * @param src connection info to the service
     * @return
     * @throws IOException
     * @throws ServiceException
     */
    public abstract Map<String, LayerCapabilities> getLayersFromService(ServiceConnectInfo src) throws IOException, ServiceException;

    /**
     * For WMS and WMTS it's more efficient to parse the layers as a group from the same service.
     * For WFS it's more efficient to only parse layers that we are interested in/have saved on the database since
     * a new HTTP request is made for each layer or featureType in WFS for DescribeFeatureType. If there are a
     * lot of feature types in a service that we don't use it's wasted bandwidth to describe ones we are not using.
     * Read: This is an optimization feature for WFS-services.
     * @return
     */
    public boolean isPreferSingleLayer() {
        return false;
    }

    /**
     * Provice a class to deserialize to from JSON. We could do:
     *
         @JsonTypeInfo(
         use = JsonTypeInfo.Id.NAME,
         property = "type")
         @JsonSubTypes({
         @JsonSubTypes.Type(value = LayerCapabilitiesWFS.class, name = "wfslayer"),
         @JsonSubTypes.Type(value = LayerCapabilitiesWMS.class, name = "wmslayer"),
         @JsonSubTypes.Type(value = LayerCapabilitiesWMTS.class, name = "wmtslayer")
         })

     * on LayerCapabilities but we would need to hardcode the subtypes there. This is more cumbersome but extendable way.
     * @return
     */
    public Class<? extends LayerCapabilities> getCapabilitiesClass() {
        return LayerCapabilities.class;
    }
    /**
     For optimization purposes to get single layer (this method can be overridden to optimize single layer, the base method is not optimized).
     For example wfs-layers require multiple requests/layer and this can be used to update single layer.
     Can be used to speed up update when service has multiple layers.
     */
    public LayerCapabilities getLayerFromService(ServiceConnectInfo src, String layer) throws IOException, ServiceException {
        if (layer == null || layer.isEmpty()) {
            throw new ServiceException("No layer specified");
        }
        Map<String, LayerCapabilities> layers = getLayersFromService(src);
        return layers.get(layer);
    }

    /**
     * Returns raw capabilities from the service as is.
     * @param capabilitiesUrl full url to the capabilties document
     * @param user credentials to use to access url
     * @param pass credentials to use to access url
     * @param expectedContentType the logic checks content type for the response
     * @return
     * @throws IOException
     * @throws ServiceException
     */
    public RawCapabilitiesResponse fetchCapabilities(String capabilitiesUrl, String user, String pass, String expectedContentType) throws IOException, ServiceException {
        String cacheKey = capabilitiesUrl + "_" + user;
        RawCapabilitiesResponse response = XML_CACHE.get(cacheKey);
        if (response != null) {
            return response;
        }
        HttpURLConnection conn = IOHelper.getConnection(capabilitiesUrl, user, pass);
        IOHelper.addIdentifierHeaders(conn);
        conn = IOHelper.followRedirect(conn, user, pass, 5);
        conn.setReadTimeout(TIMEOUT_MS);

        int sc = conn.getResponseCode();
        if (sc == HttpURLConnection.HTTP_FORBIDDEN || sc == HttpURLConnection.HTTP_UNAUTHORIZED) {
            throw new ServiceUnauthorizedException("Wrong credentials for service on " + capabilitiesUrl);
        }
        if (sc != HttpURLConnection.HTTP_OK) {
            String msg = "Unexpected status code: " + sc  + " from: " + capabilitiesUrl;
            throw new ServiceException(msg, new IOException(msg));
        }

        String contentType = conn.getContentType();
        if (contentType != null && expectedContentType != null && contentType.toLowerCase().indexOf(expectedContentType) == -1) {
            throw new ServiceException("Unexpected Content-Type: " + contentType + " from: " + capabilitiesUrl);
        }
        response = new RawCapabilitiesResponse(conn.getURL().toString());
        response.setContentType(contentType);
        String encoding = IOHelper.getCharset(conn);
        response.setResponse(IOHelper.readBytes(conn), encoding);
        XML_CACHE.put(cacheKey, response);
        return response;
    }
}
