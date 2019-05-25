package com.mika.springtxexample.config;

import lombok.SneakyThrows;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.source.ConfigurationPropertyName;
import org.springframework.boot.context.properties.source.ConfigurationPropertyNameAliases;
import org.springframework.boot.context.properties.source.ConfigurationPropertySource;
import org.springframework.boot.context.properties.source.MapConfigurationPropertySource;
import org.springframework.boot.jdbc.DatabaseDriver;
import org.springframework.boot.jdbc.XADataSourceWrapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;
import javax.sql.XADataSource;

@Configuration
public class DataSourceConfig implements BeanClassLoaderAware {
    private ClassLoader classLoader;

    @Primary
    @Bean
    @ConfigurationProperties("spring.datasource.h2")
    public DataSourceProperties h2Properties() {
        return new DataSourceProperties();
    }

    @Primary
    @Bean("h2")
    public DataSource ticketDataSource(XADataSourceWrapper wrapper) {
        return makeXaDataSource(wrapper, h2Properties());
    }

    @SneakyThrows
    private DataSource makeXaDataSource(XADataSourceWrapper wrapper, DataSourceProperties dataSourceProperties) {
        String className = dataSourceProperties.getXa().getDataSourceClassName();
        if (!StringUtils.hasLength(className)) {
            className = DatabaseDriver.fromJdbcUrl(dataSourceProperties.determineUrl()).getXaDataSourceClassName();
        }
        Assert.state(StringUtils.hasLength(className), "No XA DataSource class name specified");
        Class<?> dataSourceClass = ClassUtils.forName(className, classLoader);
        Object object = BeanUtils.instantiateClass(dataSourceClass);
        XADataSource xa = (XADataSource) object;
        Binder binder = new Binder(makeBinderSource(dataSourceProperties));
        binder.bind(ConfigurationPropertyName.EMPTY, Bindable.ofInstance(xa));
        return wrapper.wrapDataSource(xa);
    }

    private ConfigurationPropertySource makeBinderSource(DataSourceProperties dataSourceProperties) {
        MapConfigurationPropertySource source = new MapConfigurationPropertySource();
        source.put("driver-class-name", dataSourceProperties.determineDriverClassName());
        source.put("user", dataSourceProperties.determineUsername());
        source.put("password, ", dataSourceProperties.determinePassword());
        source.put("url", dataSourceProperties.determineUrl());
        source.putAll(dataSourceProperties.getXa().getProperties());
        ConfigurationPropertyNameAliases aliases = new ConfigurationPropertyNameAliases();
        aliases.addAliases("user", "username");
        return source.withAliases(aliases);
    }

    @Bean
    @ConfigurationProperties("spring.datasource.hsql")
    public DataSourceProperties hsqlProperties() {
        return new DataSourceProperties();
    }

    @Bean("hsql")
    public DataSource eventDataSource(XADataSourceWrapper wrapper) {
        return makeXaDataSource(wrapper, hsqlProperties());
    }

    @Override
    public void setBeanClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }
}
