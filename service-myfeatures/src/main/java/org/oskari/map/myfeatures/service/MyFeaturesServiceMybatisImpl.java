package org.oskari.map.myfeatures.service;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.sql.DataSource;

import fi.nls.oskari.map.geometry.ProjectionHelper;
import fi.nls.oskari.map.geometry.WKTHelper;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.geotools.api.referencing.FactoryException;
import org.geotools.api.referencing.crs.CoordinateReferenceSystem;
import org.geotools.api.referencing.operation.MathTransform;
import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.CRS;

import fi.nls.oskari.annotation.Oskari;
import fi.nls.oskari.db.DatasourceHelper;
import fi.nls.oskari.domain.map.myfeatures.MyFeaturesFeature;
import fi.nls.oskari.domain.map.myfeatures.MyFeaturesLayer;

import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.mybatis.MyBatisHelper;
import fi.nls.oskari.util.PropertyUtil;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.locationtech.jts.geom.Geometry;
import org.oskari.geojson.GeoJSON;
import org.oskari.geojson.GeoJSONWriter;

import static fi.nls.oskari.map.geometry.ProjectionHelper.getSRID;
import static fi.nls.oskari.map.geometry.WKTHelper.parseWKT;

@Oskari
public class MyFeaturesServiceMybatisImpl extends MyFeaturesService {

    private static final Logger LOG = LogFactory.getLogger(MyFeaturesServiceMybatisImpl.class);

    private static final String INSERT_BATCH_SIZE = "myfeatures.mybatis.batch.size";
    private static final String NATIVE_SRS = "oskari.native.srs";
    private static final String FALLBACK_NATIVE_SRS = "EPSG:3857";

    private final CoordinateReferenceSystem nativeCRS;
    private final int batchSize;
    private final SqlSessionFactory factory;
    private static final GeoJSONWriter geojsonWriter = new GeoJSONWriter();

    public MyFeaturesServiceMybatisImpl() {
        this(DatasourceHelper.getInstance().getDataSource(DatasourceHelper.getInstance().getOskariDataSourceName("myfeatures")));
    }

    public MyFeaturesServiceMybatisImpl(DataSource ds) {
        if (ds != null) {
            factory = initializeMyBatis(ds);
        } else {
            LOG.error("Couldn't get datasource for myfeatures");
            factory = null;
        }
        nativeCRS = createNativeCRS();
        batchSize = PropertyUtil.getOptional(INSERT_BATCH_SIZE, 1000);
    }

    private static SqlSessionFactory initializeMyBatis(final DataSource dataSource) {
        final Configuration configuration = MyBatisHelper.getConfig(dataSource);
        MyBatisHelper.addAliases(configuration, MyFeaturesLayer.class, MyFeaturesFeature.class);
        MyBatisHelper.addMappers(configuration, MyFeaturesMapper.class);
        return new SqlSessionFactoryBuilder().build(configuration);
    }

    @Override
    public CoordinateReferenceSystem getNativeCRS() {
        return nativeCRS;
    }

    @Override
    public MyFeaturesLayer getLayer(UUID layerId) {
        if (factory == null) {
            return null;
        }
        try (SqlSession session = factory.openSession()) {
            return getMapper(session).findLayer(layerId);
        }
    }

    @Override
    public void createLayer(MyFeaturesLayer layer) {
        if (layer.getId() == null) {
            layer.setId(UUID.randomUUID());
        }        
        try (SqlSession session = factory.openSession()) {
            MyFeaturesMapper mapper = getMapper(session);

            Instant now = mapper.now().toInstant();
            layer.setCreated(now);
            layer.setUpdated(now);

            mapper.insertLayer(layer);

            session.commit();
        }
    }

    @Override
    public void updateLayer(MyFeaturesLayer layer) {
        if (layer.getId() == null) {
            throw new IllegalArgumentException("Layer must have id when updating");
        }
        try (SqlSession session = factory.openSession()) {
            MyFeaturesMapper mapper = getMapper(session);

            Instant now = mapper.now().toInstant();
            layer.setUpdated(now);

            mapper.updateLayer(layer);

            session.commit();
        }
    }

    @Override
    public void deleteLayer(UUID layerId) {
        layerId = Objects.requireNonNull(layerId);
        try (SqlSession session = factory.openSession()) {
            getMapper(session).deleteLayer(layerId);
            session.commit();
        }
    }

    @Override
    public MyFeaturesFeature getFeature(UUID layerId, long featureId) {
        if (factory == null) {
            return null;
        }
        try (SqlSession session = factory.openSession()) {
            return getMapper(session).findFeatureById(featureId);
        }
    }

    @Override
    public void createFeature(UUID layerId, MyFeaturesFeature feature) {
        try (SqlSession session = factory.openSession()) {
            MyFeaturesMapper mapper = getMapper(session);

            Instant now = mapper.now().toInstant();
            feature.setCreated(now);
            feature.setUpdated(now);

            mapper.insertFeature(layerId, feature);
            mapper.refreshLayerMetadata(layerId);

            session.commit();
        }
    }

    @Override
    public void updateFeature(UUID layerId, MyFeaturesFeature feature) {
        try (SqlSession session = factory.openSession()) {
            MyFeaturesMapper mapper = getMapper(session);

            Instant now = mapper.now().toInstant();
            feature.setUpdated(now);

            mapper.updateFeature(feature);
            mapper.refreshLayerMetadata(layerId);

            session.commit();
        }
    }

    @Override
    public void deleteFeature(UUID layerId, long featureId) {
        layerId = Objects.requireNonNull(layerId);
        try (SqlSession session = factory.openSession()) {
            MyFeaturesMapper mapper = getMapper(session);
            mapper.deleteFeature(featureId);
            mapper.refreshLayerMetadata(layerId);
            session.commit();
        }
    }

    @Override
    public List<MyFeaturesFeature> getFeatures(UUID layerId) {
        if (factory == null) {
            return Collections.emptyList();
        }
        layerId = Objects.requireNonNull(layerId);
        try (SqlSession session = factory.openSession()) {
            return getMapper(session).findFeatures(layerId);
        }
    }
    
    @Override
    public List<MyFeaturesFeature> getFeaturesByBbox(UUID layerId, double minX, double minY, double maxX, double maxY) {
        if (factory == null) {
            return Collections.emptyList();
        }
        layerId = Objects.requireNonNull(layerId);
        try (SqlSession session = factory.openSession()) {
            return getMapper(session).findFeaturesByBbox(layerId, minX, minY, maxX, maxY);
        }
    }

    @Override
    public JSONObject getFeaturesAsGeoJSON(UUID layerId, String srs)  {
        try (SqlSession session = factory.openSession()) {
            MyFeaturesMapper mapper = getMapper(session);

            List<MyFeaturesFeature> features = mapper.findFeatures(layerId);
            JSONObject featureCollection = this.toGeoJSONFeatureCollection(features, srs);
            return featureCollection;
        }
    }

    private JSONObject toGeoJSONFeatureCollection(List<MyFeaturesFeature> featuresList, String targetSRSName) {
        if (featuresList == null || featuresList.isEmpty()) {
            return null;
        }
        JSONObject json = new JSONObject();
        try {
            json.put(GeoJSON.TYPE, GeoJSON.FEATURE_COLLECTION);
            json.put("crs", geojsonWriter.writeCRSObject(targetSRSName));

            JSONArray features = new JSONArray(featuresList.stream().map(feature -> this.toGeoJSONFeature(feature, targetSRSName)).collect(Collectors.toList()));
            json.put(GeoJSON.FEATURES, features);

        } catch(JSONException ex) {
            LOG.warn("Failed to create GeoJSON FeatureCollection");
        }
        return json;
    }

    private JSONObject toGeoJSONFeature(MyFeaturesFeature feature, String targetSRSName) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("id", feature.getId());
            jsonObject.put("geometry_name", GeoJSON.GEOMETRY);
            jsonObject.put(GeoJSON.TYPE, GeoJSON.FEATURE);

            String sourceSRSName = "EPSG:" + feature.getDatabaseSRID();
            Geometry transformed = WKTHelper.transform(parseWKT(feature.getGeometry().toString()), sourceSRSName, targetSRSName);
            JSONObject geoJsonGeometry = geojsonWriter.writeGeometry(transformed);
            jsonObject.put(GeoJSON.GEOMETRY, geoJsonGeometry);
            jsonObject.put("properties", feature.getProperties());

        } catch(JSONException ex) {
            LOG.warn("Failed to convert MyFeaturesFeature to GeoJSONFeature");
        }

        return jsonObject;
    }

    @Override
    public void createFeatures(UUID layerId, List<MyFeaturesFeature> features, CoordinateReferenceSystem sourceCRS) {
        layerId = Objects.requireNonNull(layerId);
        if (features.isEmpty()) {
            return;
        }
        Integer sourceSRID = 0;
        try {
            sourceSRID = CRS.lookupEpsgCode(sourceCRS, true);
        } catch (FactoryException e) {
            LOG.warn("Cannot find srid for ", sourceCRS.getCoordinateSystem().getName().toString());
        }

        try (SqlSession session = factory.openSession()) {
            MyFeaturesMapper mapper = getMapper(session);
            Instant now = mapper.now().toInstant();
            int batchCount = 0;
            for (MyFeaturesFeature feature : features) {
                feature.setCreated(now);
                feature.setUpdated(now);
                feature.getGeometry().setSRID(sourceSRID);
                mapper.insertFeature(layerId, feature);
                batchCount++;
                // Flushes batch statements and clears local session cache
                if (batchCount == batchSize) {
                    session.flushStatements();
                    session.clearCache();
                    batchCount = 0;
                }
            }
            if (batchCount > 0) {
                session.flushStatements();
            }
            mapper.refreshLayerMetadata(layerId);
            session.commit();
        }
    }

    @Override
    public List<MyFeaturesLayer> getLayersByOwnerUuid(String ownerId) {
        if (factory == null) {
            return Collections.emptyList();
        }
        try (SqlSession session = factory.openSession()) {
            return getMapper(session).findLayersByOwnerUuid(ownerId);
        }
    }

    @Override
    public void deleteLayersByOwnerUuid(String ownerUuid) {
        try (SqlSession session = factory.openSession()) {
            getMapper(session).deleteLayersByOwnerUuid(ownerUuid);
            session.commit();
        }
    }

    @Override
    public void swapAxisOrder(UUID layerId) {
        layerId = Objects.requireNonNull(layerId);
        try (SqlSession session = factory.openSession()) {
            MyFeaturesMapper mapper = getMapper(session);
            Instant now = mapper.now().toInstant();
            mapper.swapAxisOrder(layerId, now);
            mapper.refreshLayerMetadata(layerId);
            session.commit();
        }
    }

    @Override
    public void deleteFeaturesByLayerId(UUID layerId) {
        layerId = Objects.requireNonNull(layerId);
        try (SqlSession session = factory.openSession()) {
            MyFeaturesMapper mapper = getMapper(session);
            mapper.deleteFeaturesByLayerId(layerId);
            mapper.refreshLayerMetadata(layerId);
            session.commit();
        }
    }

    private static CoordinateReferenceSystem createNativeCRS() {
        try {
            String epsg = PropertyUtil.get(NATIVE_SRS, FALLBACK_NATIVE_SRS);
            return CRS.decode(epsg, true);
        } catch (Exception e) {
            LOG.error(e, "Failed to create nativeCRS!");
            return null;
        }
    }

    private MyFeaturesMapper getMapper(SqlSession session) {
	    return session.getMapper(MyFeaturesMapper.class);
	}

}
