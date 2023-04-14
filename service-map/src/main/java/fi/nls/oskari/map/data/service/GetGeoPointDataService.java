package fi.nls.oskari.map.data.service;

import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.map.data.domain.GFIRequestParams;
import fi.nls.oskari.map.data.domain.GFIRestQueryParams;
import fi.nls.oskari.util.IOHelper;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.util.XmlHelper;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;
import org.oskari.util.HtmlDoc;
import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.*;
import java.net.HttpURLConnection;
import java.util.Collections;

public class GetGeoPointDataService {

    private Logger log = LogFactory.getLogger(GetGeoPointDataService.class);
    
    public static final String TYPE = "type";
    public static final String LAYER_ID = "layerId";
    public static final String PRESENTATION_TYPE = "presentationType";
    public static final String CONTENT = "content";
    public static final String PARSED = "parsed";
    public static final String GFI_CONTENT = "gfiContent";
    public static final String KEY_INFO = "Info";
    public static final String MESSAGE_NO_SUPPORT = "Feature data not supported on this layer";
    public static final String REST_KEY_FEATURES = "features";
    public static final String REST_KEY_ATTRIBUTES = "attributes";

    public static final String PRESENTATION_TYPE_JSON = "JSON";
    public static final String PRESENTATION_TYPE_TEXT = "TEXT";

    private static final String CONFIG_KEY_FOR_SANITIZE = "gfi";

    public JSONObject getWMSFeatureInfo(final GFIRequestParams params) {

        final String gfiResponse = makeGFIcall(params.getGFIUrl(), params.getLayer().getUsername(), params.getLayer().getPassword());
        if (gfiResponse == null || gfiResponse.trim().isEmpty()) {
            return null;
        }

        final JSONObject response = new JSONObject();
        JSONHelper.putValue(response, TYPE, params.getLayer().getType());
        JSONHelper.putValue(response, LAYER_ID, params.getLayer().getId());
        // try transform if XSLT is provided
        final String xslt = params.getLayer().getGfiXslt();
        JSONObject respObj = null;
        if (xslt != null && !xslt.isEmpty()) {
            final String transformedResult = transformResponse(xslt, gfiResponse);
            respObj = JSONHelper.createJSONObject(transformedResult);
            if(respObj != null) {
                JSONHelper.putValue(response, PRESENTATION_TYPE, PRESENTATION_TYPE_JSON);
                JSONHelper.putValue(response, CONTENT, respObj);
            }
        }
        // use text content if respObj isn't present (transformed JSON not created)
        if(respObj == null) {
            JSONHelper.putValue(response, PRESENTATION_TYPE, PRESENTATION_TYPE_TEXT);
            // Note! This might not be html. Might be xml or plain text etc
            JSONHelper.putValue(response, CONTENT, getSafeHtmlContent(gfiResponse, params.getGFIUrl()));
        }
        // Add gfi content, it needs to be a separate field so we can mangle it as we like in the frontend
        final String gfiContent = params.getLayer().getGfiContent();
        if (gfiContent != null) {
            JSONHelper.putValue(response, GFI_CONTENT, gfiContent);
        }
        return response;
    }

    public static String getSafeHtmlContent(String response, String url) {
        return new HtmlDoc(response)
                .modifyLinks(url)
                .getFiltered(CONFIG_KEY_FOR_SANITIZE);
    }

    public JSONObject getRESTFeatureInfo(final GFIRestQueryParams params) {

        final String gfiResponse = makeGFIcall(params.getGFIUrl(), params.getLayer().getUsername(), params.getLayer().getPassword());
        if (gfiResponse == null || gfiResponse.trim().isEmpty()) {
            return null;
        }
        final JSONObject response = new JSONObject();
        JSONHelper.putValue(response, TYPE, params.getLayer().getType());
        JSONHelper.putValue(response, LAYER_ID, params.getLayer().getId());
        JSONHelper.putValue(response, PRESENTATION_TYPE, PRESENTATION_TYPE_JSON);
        // REST query response is in json format
        JSONObject features = new JSONObject();
        JSONArray attrs = new JSONArray();
        JSONObject respObj = JSONHelper.createJSONObject(gfiResponse);
        if (respObj != null) {
            // Pick feature data
            JSONArray feas = JSONHelper.getJSONArray(respObj, REST_KEY_FEATURES);
            if (feas == null) {
                // layer is not a REST Feature Layer
                JSONObject info = new JSONObject();
                JSONHelper.putValue(info, KEY_INFO, MESSAGE_NO_SUPPORT);
                JSONHelper.putValue(features, PARSED, info);

            } else if (feas.length() == 0) {
                // not found any features

            } else {
                // Pick attributes as features

                for (int i = 0; i < feas.length(); i++) {
                    JSONObject item = feas.optJSONObject(i);
                    attrs.put(item.optJSONObject(REST_KEY_ATTRIBUTES));
                }
                JSONHelper.putValue(features, PARSED, attrs);
            }

        }
        JSONHelper.putValue(response, CONTENT, features);

        return response;
    }

    private String makeGFIcall(final String url, final String user, final String pw) {

        try {
            log.debug("Calling GFI url:", url);
            HttpURLConnection conn = IOHelper.getConnection(url, user, pw);
            IOHelper.addIdentifierHeaders(conn);
            if (conn.getResponseCode() == HttpURLConnection.HTTP_NOT_FOUND) {
                log.debug("Nothing found on:", url);
                return null;
            }
            String gfiResponse = IOHelper.getURL(conn, Collections.EMPTY_MAP, IOHelper.DEFAULT_CHARSET);
            log.debug("Got GFI response:", gfiResponse);
            return gfiResponse;
        } catch (IOException e) {
            log.warn("Couldn't call GFI with url:", url, "Message:", e.getMessage());
            log.debug(e, "GFI IOException");
        }
        return null;
    }

    protected String transformResponse(final String xslt, final String response) {

        if (xslt == null || "".equals(xslt)) {
            // if not found, return as is
            return response;
        }

        ByteArrayInputStream respInStream = null;
        ByteArrayInputStream xsltInStream = null;
        Writer outWriter = null;
        try {
            final DocumentBuilderFactory factory = XmlHelper.newDocumentBuilderFactory();
            factory.setNamespaceAware(true);
            final DocumentBuilder builder = factory.newDocumentBuilder();

            respInStream = new ByteArrayInputStream(response.getBytes("UTF-8"));
            final Document document = builder.parse(respInStream);
            xsltInStream = new ByteArrayInputStream(xslt.getBytes());
            final StreamSource stylesource = new StreamSource(xsltInStream);
            final String transformedResponse = getFormattedJSONString(document, stylesource);
            
            if (transformedResponse == null
                    || transformedResponse.isEmpty()) {
                log.info("got empty result from transform with:", xslt, " - Response:", response);
                return response;
            }

            return transformedResponse;
        } catch (Exception e) {
            log.error("Error transforming GFI response: ", response, "- with XSLT:", xslt,
                    "Error:", e.getMessage());
        } finally {
            if (respInStream != null) {
                try {
                    respInStream.close();
                } catch (Exception ignored) {
                }
            }
            if (xsltInStream != null) {
                try {
                    xsltInStream.close();
                } catch (Exception ignored) {
                }
            }
            if (outWriter != null) {
                try {
                    outWriter.close();
                } catch (Exception ignored) {
                }
            }
        }
        // Sanitize response
        return Jsoup.clean(response, Safelist.relaxed());
    }

    public static String getFormattedJSONString(Document document, StreamSource stylesource) throws TransformerException {
        final TransformerFactory transformerFactory = XmlHelper.newTransformerFactory();
        final Transformer transformer = transformerFactory.newTransformer(stylesource);

        final DOMSource source = new DOMSource(document);
        final StringWriter outWriter = new StringWriter();
        final StreamResult result = new StreamResult(outWriter);
        transformer.transform(source, result);
        final String transformedResponse = outWriter.toString();
        return transformedResponse.trim();
    }
}