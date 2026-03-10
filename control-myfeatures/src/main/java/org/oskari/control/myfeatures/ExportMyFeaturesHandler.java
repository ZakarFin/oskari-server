package org.oskari.control.myfeatures;

import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.control.ActionConstants;
import fi.nls.oskari.control.ActionDeniedException;
import fi.nls.oskari.control.ActionException;
import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.control.ActionParamsException;
import fi.nls.oskari.control.RestActionHandler;
import fi.nls.oskari.domain.map.myfeatures.MyFeaturesFeature;
import fi.nls.oskari.domain.map.myfeatures.MyFeaturesLayer;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.map.geometry.ProjectionHelper;
import fi.nls.oskari.service.OskariComponentManager;
import jakarta.servlet.http.HttpServletResponse;

import org.geotools.api.referencing.crs.CoordinateReferenceSystem;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.geojson.feature.FeatureJSON;
import org.geotools.geojson.geom.GeometryJSON;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.CRS;
import org.oskari.geojson.BoundsWrappingSimpleFeatureCollection;
import org.oskari.geojson.GeoJSONUtil;
import org.oskari.map.myfeatures.service.MyFeaturesService;
import org.oskari.service.wfs3.CoordinateTransformer;
import org.oskari.user.User;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@OskariActionRoute("ExportMyFeaturesLayer")
public class ExportMyFeaturesHandler extends RestActionHandler {

    private static final Logger LOG = LogFactory.getLogger(ExportMyFeaturesHandler.class);
    private static final String PARAM_LAYER_ID = "layerId";
    private static final String FILE_EXT = "geojson";
    private static final String FILE_TYPE = "application/geo+json";
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private MyFeaturesService service;

    @Override
    public void init() {
        if (service == null) {
            setService(OskariComponentManager.getComponentOfType(MyFeaturesService.class));
        }
    }

    protected MyFeaturesService getService() {
        return service;
    }

    protected void setService(MyFeaturesService myFeaturesService) {
        this.service = Objects.requireNonNull(myFeaturesService);
    }

    @Override
    public void handleGet(ActionParameters params) throws ActionException {
        params.requireLoggedInUser();
        final User user = params.getUser();

        final UUID layerId = MyFeaturesLayer.parseLayerId(params.getRequiredParam(PARAM_LAYER_ID))
            .orElseThrow(() -> new ActionParamsException("Invalid " + PARAM_LAYER_ID));

        final String srs = params.getHttpParam(ActionConstants.PARAM_SRS, "EPSG:3857");
        final CoordinateReferenceSystem crs;
        try {
            crs = CRS.decode(srs, true);
        } catch (Exception e) {
            throw new ActionParamsException("Invalid " + ActionConstants.PARAM_SRS);
        }

        final MyFeaturesService service = getService();

        MyFeaturesLayer layer = service.getLayer(layerId);
        if (!canExport(user, layer)) {
            throw new ActionDeniedException("User: " + user.getId() + " tried to export features from layer " + layerId);
        }

        try {
            ByteArrayOutputStream responseBody = toGeoJSON(
                getFeatures(service, layer, crs),
                GeoJSONUtil.getNumDecimals(ProjectionHelper.isUnitDegrees(crs))
            );
            writeFileResponse(params, layer, responseBody);
        } catch (Exception e) {
            LOG.warn(e);
            throw new ActionException("Failed to export features");
        }
    }

    private boolean canExport(User user, MyFeaturesLayer layer) {
        return layer != null && layer.getOwnerUuid().equals(user.getUuid());
    }

    private static SimpleFeatureCollection getFeatures(MyFeaturesService service, MyFeaturesLayer layer, CoordinateReferenceSystem crs) throws Exception {
        List<MyFeaturesFeature> features = service.getFeatures(layer.getId());
        SimpleFeatureCollection sfc = MyFeaturesWFSHelper.convertToSimpleFeatureCollection(layer, features);
        sfc = new CoordinateTransformer(service.getNativeCRS(), crs).transform(sfc);
        // We want to write the crs information at featureCollection level. For that GeoTools FeatureJSON requires
        // non-null bounds. We don't need to write the bounds, so we just provide fake bounds with correct CRS
        return new BoundsWrappingSimpleFeatureCollection(sfc, (__) -> new ReferencedEnvelope(crs));
    }

    private static ByteArrayOutputStream toGeoJSON(SimpleFeatureCollection sfc, int decimals) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (OutputStreamWriter writer = new OutputStreamWriter(baos, StandardCharsets.UTF_8)) {
            FeatureJSON featureJSON = new FeatureJSON(new GeometryJSON(decimals));
            featureJSON.setEncodeFeatureCollectionBounds(false);
            featureJSON.setEncodeFeatureCollectionCRS(true);
            featureJSON.writeFeatureCollection(sfc, writer);
        }
        return baos;
    }

    private static void writeFileResponse(ActionParameters params, MyFeaturesLayer layer, ByteArrayOutputStream responseBody) throws IOException {
        String lang = params.getLocale().getLanguage();
        String layerName = layer.getName(lang);
        String timestamp = LocalDate.now().format(TIME_FORMAT);
        String fileName = layerName + "_" + timestamp + "." + FILE_EXT;

        HttpServletResponse response = params.getResponse();
        response.setContentType(FILE_TYPE);
        response.setHeader("Content-Disposition", "attachment; filename=" + fileName);
        response.setContentLength(responseBody.size());
        try (OutputStream out = response.getOutputStream()) {
            responseBody.writeTo(out);
        }
    }

}
