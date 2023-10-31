package org.oskari.map.userlayer.service;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import fi.nls.oskari.annotation.Oskari;
import fi.nls.oskari.db.DatasourceHelper;
import fi.nls.oskari.domain.User;
import fi.nls.oskari.service.OskariComponentManager;
import fi.nls.oskari.service.ServiceException;
import fi.nls.oskari.service.db.UserContentService;

@Oskari("userlayer")
public class UserContentUserLayerService extends UserContentService {

    private static final MetricRegistry METRIC_REGISTRY = new MetricRegistry();
    private static final String METRICS_PREFIX = "Oskari.userlayer";

    public static MetricRegistry getMetrics() {
        return METRIC_REGISTRY;
    }
    /*
    These are returning an receiving objects just so we don't need to spread references to actual timer/meter classes across the code
     */
    public static Object startTimer(String key) {
        final Timer timer = getMetrics().timer(METRICS_PREFIX + "." + key);
        Timer.Context actionTimer = timer.time();
        return actionTimer;
    }
    public static void stopTimer(Object timer) {
        if (timer instanceof Timer.Context) {
            ((Timer.Context)timer).stop();
        }
    }

    public void deleteUserContent(User user) throws ServiceException {
        if(!DatasourceHelper.isModuleEnabled(getName())) {
            return;
        }
        UserLayerDbService userLayerService = OskariComponentManager.getComponentOfType(UserLayerDbService.class);
        userLayerService.deleteUserLayersByUuid(user.getUuid());
    }
}