package fi.nls.oskari.analysis;

import fi.mml.map.mapwindow.util.OskariLayerWorker;
import fi.nls.oskari.domain.map.analysis.Analysis;
import fi.nls.oskari.domain.map.wfs.WFSLayerOptions;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.util.ConversionHelper;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.util.PropertyUtil;
import fi.nls.oskari.util.WFSConversionHelper;
import org.json.JSONArray;
import org.json.JSONObject;
import org.oskari.permissions.model.PermissionType;

/**
 * Provides utility methods for analysis
 * Moved JSON generation out of fi.nls.oskari.map.analysis.service.AnalysisDataService.
 */
public class AnalysisHelper {

    private static final String ANALYSIS_LAYERTYPE = "analysislayer";
    private static final String JSKEY_WPSLAYERID = "wpsLayerId";
    private static final String JSKEY_LAYERID = "layerId";

    private static final String JSKEY_NAME = "name";
    private static final String JSKEY_TYPE = "type";
    private static final String JSKEY_OPACITY = "opacity";
    private static final String JSKEY_MINSCALE = "minScale";
    private static final String JSKEY_MAXSCALE = "maxScale";
    private static final String JSKEY_ATTRIBUTES = "attributes";
    private static final String JSKEY_DATA = "data";
    private static final String JSKEY_FILTER = "filter";
    private static final String JSKEY_LOCALE = "locale";
    private static final String JSKEY_TYPES = "types";

    private static final String JSKEY_BBOX = "bbox";
    private static final String JSKEY_GEOM = "geom";
    private static final String JSKEY_BOTTOM = "bottom";
    private static final String JSKEY_TOP = "top";
    private static final String JSKEY_LEFT = "left";
    private static final String JSKEY_RIGHT = "right";

    private static final String JSKEY_ID = "id";
    private static final String JSKEY_WPSURL = "wpsUrl";
    private static final String JSKEY_WPSNAME = "wpsName";
    private static final String JSKEY_METHOD = "method";
    private static final String JSKEY_WPS_PARAMS = "wpsParams";
    private static final String JSKEY_NO_DATA = "no_data";
    private static final String JSKEY_METHODPARAMS = "methodParams";
    private static final String LAYER_PREFIX = "analysis_";
    private static final String JSKEY_OPTIONS = "options";

    private static final String ANALYSIS_BASELAYER_ID = PropertyUtil.get("analysis.baselayer.id");
    private static final String PROPERTY_RENDERING_URL = PropertyUtil.getOptional("analysis.rendering.url");
    private static final String ANALYSIS_RENDERING_URL = getAnalysisRenderingUrl();
    private static final String ANALYSIS_RENDERING_ELEMENT = PropertyUtil.get("analysis.rendering.element");



    private static final Logger log = LogFactory.getLogger(AnalysisHelper.class);
    /**
     * Assumes layerId in format analysis[_ignoredParts*]_[analysisId].
     * Parses analysisId or returns -1 if not able to parse it
     * @param layerId
     * @return
     */
    public static long getAnalysisIdFromLayerId(final String layerId) {
        if(!layerId.startsWith(LAYER_PREFIX)) {
            return -1;
        }
        final String[] layerIdSplitted = layerId.split("_");
        if(layerIdSplitted.length < 2) {
            return -1;
        }
        final String analysisId = layerIdSplitted[layerIdSplitted.length - 1];
        return ConversionHelper.getLong(analysisId, -1);
    }
    @Deprecated
    public static JSONObject getAnalysisPermissions(boolean hasPublish, boolean hasDownload) {

        final JSONObject permissions = new JSONObject();
        if (hasPublish) {
            JSONHelper.putValue(permissions, PermissionType.PUBLISH.getJsonKey(), OskariLayerWorker.PUBLICATION_PERMISSION_OK);
        }
        if (hasDownload) {
            JSONHelper.putValue(permissions, PermissionType.DOWNLOAD.getJsonKey(), OskariLayerWorker.DOWNLOAD_PERMISSION_OK);
        }
        return permissions;
    }
    @Deprecated
    private static JSONObject getAttributes (Analysis analysis, JSONObject analysisJSON, String lang) {
        JSONObject attributes = new JSONObject();
        JSONObject params = JSONHelper.getJSONObject(analysisJSON, JSKEY_METHODPARAMS);
        if (params != null) {
            Object noData = JSONHelper.get(params, JSKEY_NO_DATA);
            if (noData != null) {
                JSONHelper.putValue(attributes, JSKEY_WPS_PARAMS, JSONHelper.createJSONObject(JSKEY_NO_DATA, noData));
            }
        }
        String method = JSONHelper.optString(analysisJSON, JSKEY_METHOD);
        JSONHelper.putValue(attributes, JSKEY_METHOD, method);

        JSONArray filter = new JSONArray();
        JSONObject locale = new JSONObject();
        JSONObject types = new JSONObject();
        for (int j = 1; j < 11; j++) {
            String colx = analysis.getColx(j);
            if (colx != null && colx.contains("=")) {
                String[] splitted = colx.split("=");
                String field = splitted[0];
                String name = splitted[1];
                filter.put(field);
                JSONHelper.putValue(locale, field, name);
                String type = field.startsWith("n") ? WFSConversionHelper.NUMBER : WFSConversionHelper.STRING;
                JSONHelper.putValue(types, field, type);
            }
        }
        JSONObject data = new JSONObject();
        JSONHelper.put(data, JSKEY_FILTER, filter);
        JSONHelper.putValue(data, JSKEY_TYPES, types);
        JSONHelper.putValue(data, JSKEY_LOCALE, JSONHelper.createJSONObject(lang, locale));
        JSONHelper.putValue(attributes, JSKEY_DATA, data);
        return attributes;
    }

    public static String getAnalysisRenderingUrl() {
        if (PROPERTY_RENDERING_URL == null) {
            // action_route name points to fi.nls.oskari.control.layer.AnalysisTileHandler
            return PropertyUtil.get("oskari.ajax.url.prefix") + "action_route=AnalysisTile&wpsLayerId=";
        }
        return PROPERTY_RENDERING_URL + "&wpsLayerId=";
    }

    /**
     *  parse name mapped select items for analysis data select
     *  sample Analysis.getSelect_to_data()
     *  "Select  t1 As ika_0_14 t2 As kunta t3 As ika_15_64 t4 As ika_65_ t5 As miehet from analysis_data where analysis_id = 1324"
     * @param al  Analysis metadata
     * @return
     */
    public static String getAnalysisSelectItems(final Analysis al) {
        if (al == null) return null;
        if (al.getSelect_to_data() == null) return null;
        if (al.getSelect_to_data().isEmpty()) return null;
        String[] select_items = al.getSelect_to_data().split("from");
        String columns = select_items[0].substring(8);
        // Add column separators if not there
        if (columns.indexOf(",") == -1) {
            String[] parts = columns.split("[\\W]");
            StringBuilder sbuilder = new StringBuilder();
            // loop parts and add separator
            int i3 = 0;
            for (String s : parts) {
                sbuilder.append(s);
                i3++;
                if (i3 == 3) {
                    sbuilder.append(",");
                    i3 = 0;
                }
                sbuilder.append(" ");

            }
            columns = sbuilder.toString().substring(0, sbuilder.toString().length() - 2);

        }

        return columns;
    }
}
