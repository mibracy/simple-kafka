package org.example.config;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
public class DataSourceConfig {

    @Value("${sql.connector:jdbc:h2:file:../temp/h2}")
//    @Value("${ora.sql.connector:jdbc:oracle:thin:@localhost:1521:ORCLCDB}")
    public String server;

    @Value("${sql.driver:org.h2.Driver}")
//    @Value("${ora.sql.driver:oracle.jdbc.driver.OracleDriver}")
    public String driver;

    @Value("${sql.user:SA}")
//    @Value("${ora.sql.user:SYSTEM}")
    private String user;

    @Value("${sql.password:}")
//    @Value("${ora.sql.password:}")
    private String password;

    @Bean
    public DataSource getDataSource() {
        var ds = DataSourceBuilder.create();
        ds.driverClassName(driver);
        ds.url(server);
        ds.username(user);
        ds.password(password);
        return ds.build();
    }
}