package com.mika.springtxexample.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jta.atomikos.AtomikosDataSourceBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
public class DataSourceConfig {
    @Bean("h2")
    @ConfigurationProperties("spring.datasource.h2")
    public DataSource ticketDataSource() {
        return new AtomikosDataSourceBean();
    }

    @Bean("hsql")
    @ConfigurationProperties("spring.datasource.hsql")
    public DataSource eventDataSource() {
        return new AtomikosDataSourceBean();
    }

}
