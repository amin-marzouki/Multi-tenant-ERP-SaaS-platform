package com.orbiterp.tenant;

import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class DataSourceConfig {

    /** The routing data source everything uses (JPA, Flyway), backed by the shared database. */
    @Bean
    @Primary
    public DataSource dataSource(DataSourceProperties properties) {
        DataSource shared = properties.initializeDataSourceBuilder().build();

        // Tenants with a dedicated database go here: tenant id -> DataSource. None for the MVP.
        Map<Object, Object> dedicated = new HashMap<>();

        TenantRoutingDataSource routing = new TenantRoutingDataSource();
        routing.setDefaultTargetDataSource(shared);
        routing.setTargetDataSources(dedicated);
        routing.setLenientFallback(true);
        return routing;
    }
}
