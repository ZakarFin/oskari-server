package fi.nls.oskari.csw.worker;

import fi.nls.oskari.annotation.Oskari;
import fi.nls.oskari.csw.dao.OskariLayerMetadataDao;
import fi.nls.oskari.csw.helper.CSW;
import fi.nls.oskari.csw.helper.CSW.RefreshResult;
import fi.nls.oskari.db.DatasourceHelper;
import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.map.layer.OskariLayerService;
import fi.nls.oskari.map.layer.OskariLayerServiceMybatisImpl;
import fi.nls.oskari.worker.ScheduledJob;

import javax.sql.DataSource;
import java.util.*;

/**
 * Scheduled job for retrieving coverage data for maplayers having metadataids.
 */
@Oskari("CSWCoverageImport")
public class CSWCoverageUpdateService extends ScheduledJob {
    private static final Logger log = LogFactory.getLogger(CSWCoverageUpdateService.class);

    final OskariLayerService layerService = new OskariLayerServiceMybatisImpl();

    @Override
    public void execute(Map<String, Object> params) {
        log.info("Starting the CSW coverage update service call...");

        final DataSource dataSource = getDatasource();
        if (dataSource == null) {
            return;
        }
        final OskariLayerMetadataDao dao = new OskariLayerMetadataDao(dataSource);

        int attemptCount = 0;
        int updateCount = 0;
        for (OskariLayer layer : layerService.findAll()) {
            RefreshResult result = CSW.refreshLayerMetadata(dao, layer);
            if (result != RefreshResult.SKIPPED) {
                attemptCount++;
            }
            if (result == RefreshResult.OK) {
                updateCount++;
            }
        }
        log.info("Done with the CSW coverage update service call. Updated:", updateCount, "/", attemptCount, "metadata geometries.");
    }

    private DataSource getDatasource() {
        try {
            return DatasourceHelper.getInstance().getDataSource();
        }
        catch (Exception ex) {
            log.error(ex, "Couldn't get datasource");
        }
        return null;
    }

}