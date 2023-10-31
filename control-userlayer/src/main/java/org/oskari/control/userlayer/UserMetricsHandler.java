package org.oskari.control.userlayer;

import com.codahale.metrics.MetricFilter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.json.MetricsModule;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.control.*;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.util.ResponseHelper;
import org.oskari.map.userlayer.service.UserContentUserLayerService;

import java.io.StringWriter;
import java.util.concurrent.TimeUnit;

@OskariActionRoute("UserMetrics")
public class UserMetricsHandler extends RestActionHandler {

    private static final Logger LOG = LogFactory.getLogger(UserMetricsHandler.class);

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper().registerModule(
            new MetricsModule(TimeUnit.SECONDS, TimeUnit.SECONDS, true, MetricFilter.ALL));

    @Override
    public void handleGet(ActionParameters params) throws ActionException {
        // only available for admins
        params.requireAdminUser();

        // dropwizard metrics
        MetricRegistry metrics = UserContentUserLayerService.getMetrics();

        ObjectWriter writer = OBJECT_MAPPER.writerWithDefaultPrettyPrinter();
        StringWriter w = new StringWriter();
        try {
            writer.writeValue(w, metrics);
        } catch (Exception e) {
            LOG.error(e, "Error writing metrics JSON");
        }

        ResponseHelper.writeResponse(params, JSONHelper.createJSONObject(w.toString()));
    }


    @Override
    public void preProcess(ActionParameters params) throws ActionException {
        if (!params.getUser().isAdmin()) {
            throw new ActionDeniedException("Admin only");
        }
    }

}