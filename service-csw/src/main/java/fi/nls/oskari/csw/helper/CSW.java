package fi.nls.oskari.csw.helper;

import org.json.JSONObject;
import org.oskari.capabilities.ogc.LayerCapabilitiesOGC;

import fi.nls.oskari.csw.dao.OskariLayerMetadataDao;
import fi.nls.oskari.csw.domain.CSWIsoRecord;
import fi.nls.oskari.csw.dto.OskariLayerMetadataDto;
import fi.nls.oskari.csw.service.CSWService;
import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.service.ServiceException;
import fi.nls.oskari.util.PropertyUtil;

public class CSW {

    private static final Logger LOG = LogFactory.getLogger(CSW.class);

    private static final String LAYER_ATTRIBUTE_METADATA_URL = "metadataUrl";

    private CSW() {
    }

    public static RefreshResult refreshLayerMetadata(OskariLayerMetadataDao metadataDato, OskariLayer layer) {
        String baseURL = getMetadataServiceBaseURL(layer.getAttributes());
        String metadataid = getMetadataIdForLayer(layer);
        String lang = PropertyUtil.getDefaultLanguage();

        if (metadataid == null || metadataid.isBlank()) {
            return RefreshResult.SKIPPED;
        }

        try {
            CSWIsoRecord rec = getCSWRecord(baseURL, metadataid, lang);
            if (rec == null) {
                return RefreshResult.RECORD_NOT_FOUND;
            }

            String json = rec.toJSON().toString();
            String wkt = rec.getIdentifications().stream().findAny()
                    .map(x -> x.getExtents().getEnvelope().toText())
                    .orElse(null);
            if (wkt == null) {
                return RefreshResult.GEOMETRY_NOT_FOUND;
            }

            OskariLayerMetadataDto dto = new OskariLayerMetadataDto();
            dto.metadataId = metadataid;
            dto.json = json;
            dto.wkt = wkt;
            metadataDato.saveMetadata(dto);

            return RefreshResult.OK;
        } catch (Exception e) {
            LOG.warn(e, "CSW metadata handling failed, baseURL", baseURL, "id", metadataid);
            return RefreshResult.FAILED;
        }
    }

    public enum RefreshResult {
        SKIPPED,
        RECORD_NOT_FOUND,
        GEOMETRY_NOT_FOUND,
        OK,
        FAILED
    }

    public static String getMetadataServiceBaseURL(JSONObject layerAttributes) {
        return layerAttributes != null && layerAttributes.has(LAYER_ATTRIBUTE_METADATA_URL)
                ? layerAttributes.get(LAYER_ATTRIBUTE_METADATA_URL).toString()
                : getDefaultServiceBaseURL();
    }

    public static String getDefaultServiceBaseURL() {
        return PropertyUtil.getOptional(CSWService.PROP_SERVICE_URL);
    }

    public static String getMetadataIdForLayer(OskariLayer layer) {
        String uuid = layer.getMetadataId();
        if (uuid != null && !uuid.trim().isEmpty()) {
            // override metadataid
            return uuid.trim();
        }
        // uuid from capabilities
        return layer.getCapabilities().optString(LayerCapabilitiesOGC.METADATA_UUID, null);
    }

    public static CSWIsoRecord getCSWRecord(String serviceUrl, String metadataid, String lang) throws ServiceException {
        CSWService service;
        try {
            service = new CSWService(serviceUrl);
        } catch (Exception e) {
            throw new ServiceException("Failed to initialize CSWService:" + e.getMessage());
        }

        try {
            return service.getRecordById(metadataid, lang);
        } catch (Exception e) {
            throw new ServiceException("Failed to query service: " + e.getMessage());
        }
    }

}
