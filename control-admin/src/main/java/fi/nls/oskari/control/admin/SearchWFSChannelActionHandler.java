package fi.nls.oskari.control.admin;

import fi.mml.map.mapwindow.util.OskariLayerWorker;
import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.control.ActionException;
import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.control.RestActionHandler;
import fi.nls.oskari.search.channel.WFSChannelHandler;
import fi.nls.oskari.service.OskariComponentManager;
import fi.nls.oskari.util.ConversionHelper;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.util.PropertyUtil;
import fi.nls.oskari.util.ResponseHelper;
import org.oskari.log.AuditLog;
import fi.nls.oskari.wfs.WFSSearchChannelsConfiguration;
import fi.nls.oskari.wfs.WFSSearchChannelsService;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static fi.nls.oskari.control.ActionConstants.PARAM_SRS;

@OskariActionRoute("SearchWFSChannel")
public class SearchWFSChannelActionHandler extends RestActionHandler {

    private static final String PARAM_ID = "id";
    private static final String PARAM_WFS_ID = "wfsLayerId";
    private static final String PARAM_LOCALE = "locale";
    private static final String PARAM_PARAMS_FOR_SEARCH = "paramsForSearch";
    private static final String PARAM_IS_DEFAULT = "isDefault";
    private static final String PARAM_CONFIG = "config";

    private WFSSearchChannelsService channelService;

    @Override
    public void init() {
        super.init();
        channelService = OskariComponentManager.getComponentOfType(WFSSearchChannelsService.class);
    }

    @Override
    public void preProcess(ActionParameters params)
            throws ActionException {
        super.preProcess(params);
        // Only admin user
        params.requireAdminUser();
    }

    @Override
    public void handleGet(ActionParameters params)
            throws ActionException {

        JSONObject response = new JSONObject();
        JSONArray channelsJSONArray = new JSONArray();
        try {
            for(WFSSearchChannelsConfiguration channel : channelService.findChannels()) {
                JSONObject channelJSON = channel.getAsJSONObject();
                List<Integer> layerIds = Collections.singletonList(channel.getWFSLayerId());
                JSONObject userLayers = OskariLayerWorker.getListOfMapLayersByIdList(layerIds, params.getUser(), params.getLocale().getLanguage(), params.getHttpParam(PARAM_SRS));
                JSONArray layers = userLayers.getJSONArray(OskariLayerWorker.KEY_LAYERS);

                if(layers.length() > 0){
                    channelsJSONArray.put(channelJSON);
                }
            }
        } catch (Exception ex){
            throw new ActionException("Couldn't get WFS search channels", ex);
        }
        JSONHelper.putValue(response, "channels", channelsJSONArray);
        JSONArray handlers = new JSONArray();
        Map<String, WFSChannelHandler> handlerMap =
                OskariComponentManager.getComponentsOfType(WFSChannelHandler.class);
        for(String id: handlerMap.keySet()) {
            handlers.put(id);
        }
        JSONHelper.putValue(response, "handlers", handlers);
        ResponseHelper.writeResponse(params, response);
    }

    @Override
    public void handleDelete(ActionParameters params)
            throws ActionException {
        int channelId = ConversionHelper.getInt(params.getRequiredParam(PARAM_ID), -1);

        try {
            JSONObject response = new JSONObject();
            channelService.delete(channelId);

            AuditLog.user(params.getClientIp(), params.getUser())
                    .withParam("id", channelId)
                    .withMsg("WFS Search channel")
                    .deleted(AuditLog.ResourceType.SEARCH);
            JSONHelper.putValue(response, "success", true);
            ResponseHelper.writeResponse(params, response);
        } catch (Exception ex) {
            throw new ActionException("Couldn't delete WFS search channel", ex);
        }
    }

    @Override
    public void handlePost(ActionParameters params)
            throws ActionException {

        try {
            WFSSearchChannelsConfiguration conf = parseConfig(params);
            conf.setId(ConversionHelper.getInt(params.getRequiredParam(PARAM_ID), -1));

            JSONObject response = new JSONObject();
            channelService.update(conf);

            AuditLog.user(params.getClientIp(), params.getUser())
                    .withParam("id", conf.getId())
                    .withParam("name", conf.getName(PropertyUtil.getDefaultLanguage()))
                    .withMsg("WFS Search channel")
                    .updated(AuditLog.ResourceType.SEARCH);
            JSONHelper.putValue(response, "success", true);
            ResponseHelper.writeResponse(params, response);
        } catch (Exception ex) {
            throw new ActionException("Couldn't update WFS search channel", ex);
        }
    }

    @Override
    public void handlePut(ActionParameters params)
            throws ActionException {

        try {
            WFSSearchChannelsConfiguration conf = parseConfig(params);
            long newId = channelService.insert(conf);
            AuditLog.user(params.getClientIp(), params.getUser())
                    .withParam("id", conf.getId())
                    .withParam("name", conf.getName(PropertyUtil.getDefaultLanguage()))
                    .withMsg("WFS Search channel")
                    .added(AuditLog.ResourceType.SEARCH);
            JSONObject response = new JSONObject();
            JSONHelper.putValue(response, "success", newId > 0);
            ResponseHelper.writeResponse(params, response);
        } catch (Exception ex) {
            throw new ActionException("Couldn't add WFS search channel", ex);
        }
    }

    private WFSSearchChannelsConfiguration parseConfig(ActionParameters params)
            throws Exception {

        WFSSearchChannelsConfiguration conf = new WFSSearchChannelsConfiguration();
        conf.setWFSLayerId(ConversionHelper.getInt(params.getRequiredParam(PARAM_WFS_ID), -1));
        conf.setIsDefault(ConversionHelper.getBoolean(params.getRequiredParam(PARAM_IS_DEFAULT), false));
        conf.setParamsForSearch(new JSONArray(params.getRequiredParam(PARAM_PARAMS_FOR_SEARCH)));
        conf.setLocale(new JSONObject(params.getRequiredParam(PARAM_LOCALE)));
        conf.setConfig(new JSONObject(params.getRequiredParam(PARAM_CONFIG)));
        return conf;
    }
}
