package com.orbiterp.tenant;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

import javax.sql.DataSource;

/**
 * Picks the database of the current tenant. For the MVP every tenant uses the shared default
 * database; giving one tenant its own database later only means adding it to the target map
 * (tenant id to DataSource) in {@link DataSourceConfig}.
 *
 * <p>The connection is chosen when a transaction starts, so the tenant must be set first.
 */
public class TenantRoutingDataSource extends AbstractRoutingDataSource implements DisposableBean {

    @Override
    protected Object determineCurrentLookupKey() {
        return TenantContext.get();
    }

    @Override
    public void destroy() throws Exception {
        for (DataSource dataSource : getResolvedDataSources().values()) {
            close(dataSource);
        }
        close(getResolvedDefaultDataSource());
    }

    private static void close(DataSource dataSource) throws Exception {
        if (dataSource instanceof AutoCloseable closeable) {
            closeable.close();
        }
    }
}
