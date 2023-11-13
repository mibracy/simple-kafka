package org.example.config;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
public class DataSourceConfig {

    @Value("${sql.connector:jdbc:h2:file:../temp/h2}")
    public String server;

    @Value("${sql.password:}")
    private String password;
    @Bean
    public DataSource getDataSource() {
        DataSourceBuilder<?> ds = DataSourceBuilder.create();
        ds.driverClassName("org.h2.Driver");
        ds.url(server);
        ds.username("SA");
        ds.password(password);
        return ds.build();
    }
}