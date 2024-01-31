package fi.nls.oskari.map.style;

import fi.nls.oskari.annotation.Oskari;
import fi.nls.oskari.db.DatasourceHelper;
import fi.nls.oskari.domain.User;
import fi.nls.oskari.domain.map.DataProvider;
import fi.nls.oskari.domain.map.style.VectorStyle;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.map.layer.DataProviderMapper;
import fi.nls.oskari.mybatis.MyBatisHelper;
import fi.nls.oskari.service.ServiceRuntimeException;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import javax.sql.DataSource;
import java.nio.file.AccessDeniedException;
import java.util.List;

@Oskari
public class VectorStyleServiceMybatisImpl extends VectorStyleService {
    private static final Logger log = LogFactory.getLogger(VectorStyleServiceMybatisImpl.class);
    private SqlSessionFactory factory;

    public VectorStyleServiceMybatisImpl() {
        final DatasourceHelper helper = DatasourceHelper.getInstance();
        DataSource dataSource = helper.getDataSource();
        if (dataSource == null) {
            dataSource = helper.createDataSource();
        }
        if (dataSource == null) {
            log.error("Couldn't get datasource for vector style service");
        }
        factory = initializeMyBatis(dataSource);
    }
    private SqlSessionFactory initializeMyBatis(final DataSource dataSource) {
        final Configuration configuration = MyBatisHelper.getConfig(dataSource);
        MyBatisHelper.addAliases(configuration, VectorStyle.class);
        MyBatisHelper.addMappers(configuration, VectorStyleMapper.class);

        return new SqlSessionFactoryBuilder().build(configuration);
    }
    public VectorStyle getDefaultStyle() {
        try (final SqlSession session = factory.openSession()) {
            final VectorStyleMapper mapper = session.getMapper(VectorStyleMapper.class);
            return mapper.getDefaultStyle();
        } catch (Exception e) {
            log.warn("Failed to get instance default vector style");
        }
        return null;
    }
    public VectorStyle getStyleById(final long id) {
        try (final SqlSession session = factory.openSession()) {
            final VectorStyleMapper mapper = session.getMapper(VectorStyleMapper.class);
            return mapper.getStyleById(id);
        } catch (Exception e) {
            throw new ServiceRuntimeException("Failed to get vector style for id: " + id, e);
        }
    }
    public List<VectorStyle> getStylesByUser(final long user) {
        try (final SqlSession session = factory.openSession()) {
            final VectorStyleMapper mapper = session.getMapper(VectorStyleMapper.class);
            return mapper.getStylesByUser(user);
        } catch (Exception e) {
            throw new ServiceRuntimeException("Failed to get vector styles for user: " + user, e);
        }
    }
    public List<VectorStyle> getStyles(final long userId, final int layerId) {
        try (final SqlSession session = factory.openSession()) {
            final VectorStyleMapper mapper = session.getMapper(VectorStyleMapper.class);
            return mapper.getStyles(userId, layerId);
        } catch (Exception e) {
            throw new ServiceRuntimeException("Failed to get vector styles for layer: " + layerId, e);
        }
    }
    public boolean hasPermissionToAlter(final long id, final User user) {
        try (final SqlSession session = factory.openSession()) {
            final VectorStyleMapper mapper = session.getMapper(VectorStyleMapper.class);
            long userId = mapper.getUserId(id);
            return user.getId() == userId;
        } catch (Exception e) {
            throw new ServiceRuntimeException("Failed to check vector style permission for id: " + id, e);
        }
    }
    public void deleteStyle(final long id) {
        try (final SqlSession session = factory.openSession()) {
            final VectorStyleMapper mapper = session.getMapper(VectorStyleMapper.class);
            mapper.deleteStyle(id);
            session.commit();
        } catch (Exception e) {
            throw new ServiceRuntimeException("Failed to delete vector style: " + id, e);
        }
    }
    public long saveStyle(final VectorStyle style) {
        try (final SqlSession session = factory.openSession()) {
            if (style.getLayerId() == null) {
                // layer_id column is nullable because instance default style doesn't have layerId
                throw new IllegalArgumentException("Tried to add vector style without layerId");
            }
            final VectorStyleMapper mapper = session.getMapper(VectorStyleMapper.class);
            mapper.saveStyle(style);
            session.commit();
            return style.getId();
        } catch (Exception e) {
            throw new ServiceRuntimeException("Failed to save vector style", e);
        }
    }
    public long updateStyle(final VectorStyle style) {
        try (final SqlSession session = factory.openSession()) {
            final VectorStyleMapper mapper = session.getMapper(VectorStyleMapper.class);
            mapper.updateStyle(style);
            session.commit();
            return style.getId();
        } catch (Exception e) {
            throw new ServiceRuntimeException("Failed to update vector style", e);
        }
    }
    public List<VectorStyle> getAdminStyles(final int layerId) {
        try (final SqlSession session = factory.openSession()) {
            final VectorStyleMapper mapper = session.getMapper(VectorStyleMapper.class);
            return mapper.getAdminStyles(layerId);
        } catch (Exception e) {
            throw new ServiceRuntimeException("Failed to get vector styles for layer: " + layerId, e);
        }
    }
    public void deleteAdminStyle(final long id) {
        try (final SqlSession session = factory.openSession()) {
            final VectorStyleMapper mapper = session.getMapper(VectorStyleMapper.class);
            VectorStyle found = getStyleById(id);
            if (found != null && found.getCreator() != null) {
                throw new AccessDeniedException("Tried to delete non-admin style");
            }
            mapper.deleteStyle(id);
            session.commit();
        } catch (Exception e) {
            throw new ServiceRuntimeException("Failed to delete vector style: " + id, e);
        }
    }
    public long saveAdminStyle(final VectorStyle style) {
        try (final SqlSession session = factory.openSession()) {
            if (style.getLayerId() == null) {
                // layer_id column is nullable because instance default style doesn't have layerId
                throw new IllegalArgumentException("Tried to add vector style without layerId");
            }
            if (style.getCreator() != null) {
                log.warn("Tried to add admin style with userId: " + style.getCreator() + ". Updated to null.");
                style.setCreator(null);
            }
            final VectorStyleMapper mapper = session.getMapper(VectorStyleMapper.class);
            mapper.saveStyle(style);
            session.commit();
            return style.getId();
        } catch (Exception e) {
            throw new ServiceRuntimeException("Failed to save vector style", e);
        }
    }
    public long updateAdminStyle(final VectorStyle style) {
        try (final SqlSession session = factory.openSession()) {
            if (style.getCreator() != null) {
                log.warn("Tried to update admin style with userId: " + style.getCreator() + ". Updated to null.");
                style.setCreator(null);
            }
            final VectorStyleMapper mapper = session.getMapper(VectorStyleMapper.class);
            mapper.updateStyle(style);
            session.commit();
            return style.getId();
        } catch (Exception e) {
            throw new ServiceRuntimeException("Failed to update vector style", e);
        }
    }
}
