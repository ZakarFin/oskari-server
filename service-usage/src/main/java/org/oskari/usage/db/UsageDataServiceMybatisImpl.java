package org.oskari.usage.db;

import fi.nls.oskari.db.DatasourceHelper;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.util.OskariRuntimeException;
import org.oskari.usage.UsageData;
import org.oskari.usage.UsageDataService;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.TransactionFactory;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;

import javax.sql.DataSource;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class UsageDataServiceMybatisImpl extends UsageDataService {

    private static final Logger LOG = LogFactory.getLogger(UsageDataServiceMybatisImpl.class);

    private SqlSessionFactory factory = null;

    public UsageDataServiceMybatisImpl() {

        final DatasourceHelper helper = DatasourceHelper.getInstance();
        final DataSource dataSource = helper.getDataSource(helper.getOskariDataSourceName("ratings"));
        if(dataSource != null) {
            factory = initializeMyBatis(dataSource);
        }
        else {
            LOG.error("Couldn't get datasource for ratings");
        }
    }

    private SqlSessionFactory initializeMyBatis(final DataSource dataSource) {
        final TransactionFactory transactionFactory = new JdbcTransactionFactory();
        final Environment environment = new Environment("development", transactionFactory, dataSource);

        final Configuration configuration = new Configuration(environment);
        configuration.getTypeAliasRegistry().registerAlias(UsageData.class);
        configuration.setLazyLoadingEnabled(true);
        configuration.addMapper(UsageDataMapper.class);

        return new SqlSessionFactoryBuilder().build(configuration);
    }

    public void recordUsage(int maplayer) {
        // TODO: bump if exists, insert if not
        UsageData data = new UsageData();
        data.setDate(new Date());
        data.setMaplayer(maplayer);
        // data.setMaplayer_name();
        int count = getDailyTotal(maplayer, data.getDate());
        data.setTotal(count + 1);
        if (count == 1) {
            insert(data);
        } else {
            update(data);
        }
    }

    public void save(UsageData data) {
        // TODO: bump if exists, insert if not
        int count = getDailyTotal(data.getMaplayer(), new Date());
        if (count == -1) {
            insert(data);
        } else {
            update(data);
        }
    }

    private void insert(UsageData data) {
        try (final SqlSession session = factory.openSession()) {
            final UsageDataMapper mapper = session.getMapper(UsageDataMapper.class);
            mapper.insert(data);
            session.commit();
        } catch (Exception e) {
            throw new OskariRuntimeException("Exception trying to insert", e);
        }
    }

    private void update(UsageData data) {
        try (final SqlSession session = factory.openSession()) {
            final UsageDataMapper mapper = session.getMapper(UsageDataMapper.class);
            mapper.update(data);
            session.commit();
        } catch (Exception e) {
            throw new OskariRuntimeException("Exception trying to update", e);
        }
    }

    public int getDailyTotal(int maplayerId, Date date) {
        try (final SqlSession session = factory.openSession()) {
            final UsageDataMapper mapper = session.getMapper(UsageDataMapper.class);
            return mapper.getTotal(maplayerId, date);
        } catch (Exception e) {
            LOG.error(e, "Failed to load ratings");
        }
        return 0;
    }

    public List<UsageData> getData(int maplayerId, Date start, Date end) {
        try (final SqlSession session = factory.openSession()) {
            final UsageDataMapper mapper = session.getMapper(UsageDataMapper.class);
            return mapper.getData(maplayerId, start, end);
        } catch (Exception e) {
            LOG.error(e, "Failed to load ratings");
        }
        return Collections.emptyList();
    }
}
