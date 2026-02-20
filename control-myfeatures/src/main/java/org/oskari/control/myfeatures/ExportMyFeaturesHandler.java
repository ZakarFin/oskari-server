package org.oskari.control.myfeatures;

import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.control.ActionException;
import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.control.RestActionHandler;
import fi.nls.oskari.domain.map.myfeatures.MyFeaturesLayer;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.service.OskariComponentManager;
import fi.nls.oskari.util.PropertyUtil;
import jakarta.servlet.http.HttpServletResponse;
import org.json.JSONObject;
import org.oskari.map.myfeatures.service.MyFeaturesService;
import org.oskari.user.User;
import java.io.BufferedWriter;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.UUID;

@OskariActionRoute("ExportMyFeaturesLayer")
public class ExportMyFeaturesHandler extends RestActionHandler {
    private final static Logger LOG = LogFactory.getLogger(ExportMyFeaturesHandler.class);
    private static final String PARAM_SRS = "srs";
    private static final String PARAM_LAYER_ID = "layerId";
    private static final String PARAM_INDENT = "indent";
    private static final String FILE_EXT = "geojson";
    private static final String FILE_TYPE = "application/json";

    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private MyFeaturesService service;

    @Override
    public void init() {
        if (service == null) {
            setService(OskariComponentManager.getComponentOfType(MyFeaturesService.class));
        }
    }

    void setService(MyFeaturesService myFeaturesService) {
        this.service = Objects.requireNonNull(myFeaturesService);
    }

    @Override
    public void handleGet(ActionParameters params) throws ActionException {
        final User user = params.getUser();
        final String srs = params.getHttpParam(PARAM_SRS, PropertyUtil.get("oskari.native.srs", "EPSG:4326"));
        final UUID layerId = MyFeaturesLayer.parseLayerId(params.getRequiredParam(PARAM_LAYER_ID)).orElse(null);
        final int indent = params.getHttpParam(PARAM_INDENT, -1);
        final boolean prettify = indent > 0 && indent <= 8;
        try {

            JSONObject featureCollection = getService().getFeaturesAsGeoJSON(layerId, srs);
            String layerName = getService().getLayer(layerId).getName("fi");
            String timestamp = LocalDate.now().format(TIME_FORMAT);
            String fileName = layerName + "_" + timestamp + "." + FILE_EXT;
            HttpServletResponse response = params.getResponse();
            response.setContentType(FILE_TYPE);
            response.setHeader("Content-Disposition", "attachment; filename=" + fileName);

            try (OutputStream out = response.getOutputStream()) {
                try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(out))) {
                    String stringified = prettify ? featureCollection.toString(indent) : featureCollection.toString();
                    bw.write(stringified);
                }
            } catch (Exception e) {
                LOG.warn(e);
                throw new ActionException("Failed to write JSON");
            }
        } catch (Exception e) {
            LOG.warn(e);
            throw new ActionException("Failed to export features");
        }
    }


    protected MyFeaturesService getService() {
        return service;
    }

}
