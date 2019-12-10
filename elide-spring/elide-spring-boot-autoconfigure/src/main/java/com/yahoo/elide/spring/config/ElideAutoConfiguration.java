/*
 * Copyright 2019, Yahoo Inc.
 * Licensed under the Apache License, Version 2.0
 * See LICENSE file in project root for terms.
 */
package com.yahoo.elide.spring.config;

import com.yahoo.elide.Elide;
import com.yahoo.elide.ElideSettingsBuilder;
import com.yahoo.elide.audit.Slf4jLogger;
import com.yahoo.elide.contrib.swagger.SwaggerBuilder;
import com.yahoo.elide.core.DataStore;
import com.yahoo.elide.core.EntityDictionary;
import com.yahoo.elide.core.filter.dialect.RSQLFilterDialect;
import com.yahoo.elide.datastores.aggregation.AggregationDataStore;
import com.yahoo.elide.datastores.aggregation.QueryEngineFactory;
import com.yahoo.elide.datastores.aggregation.metadata.MetaDataStore;
import com.yahoo.elide.datastores.aggregation.queryengines.sql.SQLQueryEngineFactory;
import com.yahoo.elide.datastores.jpa.JpaDataStore;
import com.yahoo.elide.datastores.jpa.transaction.NonJtaTransaction;
import com.yahoo.elide.datastores.multiplex.MultiplexManager;

import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.AbstractJpaVendorAdapter;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;

import io.swagger.models.Info;
import io.swagger.models.Swagger;

import java.util.HashMap;
import java.util.TimeZone;
import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

/**
 * Auto Configuration For Elide Services.  Override any of the beans (by defining your own) to change
 * the default behavior.
 */
@Configuration
@EnableConfigurationProperties(ElideConfigProperties.class)
public class ElideAutoConfiguration {

    /**
     * Creates the Elide instance with standard settings.
     * @param dictionary Stores the static metadata about Elide models.
     * @param dataStore The persistence store.
     * @param settings Elide settings.
     * @return A new elide instance.
     */
    @Bean
    @ConditionalOnMissingBean
    public Elide initializeElide(EntityDictionary dictionary,
                          DataStore dataStore, ElideConfigProperties settings) {

        ElideSettingsBuilder builder = new ElideSettingsBuilder(dataStore)
                .withEntityDictionary(dictionary)
                .withDefaultMaxPageSize(settings.getMaxPageSize())
                .withDefaultPageSize(settings.getPageSize())
                .withUseFilterExpressions(true)
                .withJoinFilterDialect(new RSQLFilterDialect(dictionary))
                .withSubqueryFilterDialect(new RSQLFilterDialect(dictionary))
                .withAuditLogger(new Slf4jLogger())
                .withEncodeErrorResponses(true)
                .withISO8601Dates("yyyy-MM-dd'T'HH:mm'Z'", TimeZone.getTimeZone("UTC"));

        return new Elide(builder.build());
    }

    /**
     * Creates the entity dictionary for Elide which contains static metadata about Elide models.
     * Override to load check classes or life cycle hooks.
     * @param beanFactory Injector to inject Elide models.
     * @return a newly configured EntityDictionary.
     */
    @Bean
    @ConditionalOnMissingBean
    public EntityDictionary buildDictionary(AutowireCapableBeanFactory beanFactory) {
        return new EntityDictionary(new HashMap<>(), beanFactory::autowireBean);
    }

    /**
     * Creates the DataStore Elide.  Override to use a different store.
     * @param entityManagerFactory The JPA factory which creates entity managers.
     * @return An instance of a JPA DataStore.
     */
    @Bean
    @ConditionalOnMissingBean
    public DataStore buildDataStore(EntityManagerFactory entityManagerFactory,
                                    QueryEngineFactory queryEngineFactory,
                                    ElideConfigProperties settings) throws ClassNotFoundException {
        String packageName = settings.getModelPackage();
        MetaDataStore metaDataStore = new MetaDataStore(packageName);

        AggregationDataStore aggregationDataStore = new AggregationDataStore(queryEngineFactory, metaDataStore);

        JpaDataStore jpaDataStore = new JpaDataStore(
                () -> { return entityManagerFactory.createEntityManager(); },
                    (em -> { return new NonJtaTransaction(em); }));

        // meta data store needs to be put at first to populate meta data models
        return new MultiplexManager(jpaDataStore, metaDataStore, aggregationDataStore);
    }

    /**
     * Creates a singular swagger document for JSON-API.
     * @param dictionary Contains the static metadata about Elide models.
     * @param settings Elide configuration settings.
     * @return An instance of a JPA DataStore.
     */
    @Bean
    @ConditionalOnMissingBean
    public Swagger buildSwagger(EntityDictionary dictionary, ElideConfigProperties settings) {
        Info info = new Info()
                .title(settings.getSwagger().getName())
                .version(settings.getSwagger().getVersion());

        SwaggerBuilder builder = new SwaggerBuilder(dictionary, info);

        Swagger swagger = builder.build().basePath(settings.getJsonApi().getPath());

        return swagger;
    }

    /**
     * Configure the QueryEngineFactory that the Aggregation Data Store uses.
     * @param entityManagerFactory Needed by the SQLQueryEngine
     * @return a SQLQueryEngineFactory
     */
    @Bean
    @ConditionalOnMissingBean
    public QueryEngineFactory buildQueryEngineFactory(EntityManagerFactory entityManagerFactory) {
        return new SQLQueryEngineFactory(entityManagerFactory);
    }


    /**
     * Configure the EntityManagerFactory used by Elide.  Elide model packages are limited to those specified
     * in the elide configuration.  By default, the factory uses the Spring JPA settings that are configured
     * in the application properties (prefix 'spring.jpa').  The factory also uses the default data source.
     * @param jpaSettings Spring JPA Settings.
     * @param elideSettings Elide Settings
     * @param dataSource Default Data Source.
     * @return A new EntityManagerFactory bean.
     */
    @Bean
    @ConditionalOnMissingBean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(JpaProperties jpaSettings,
                                                                       ElideConfigProperties elideSettings,
                                                                       DataSource dataSource) {

        LocalContainerEntityManagerFactoryBean emf = new LocalContainerEntityManagerFactoryBean();
        emf.setPackagesToScan(new String[]{elideSettings.getModelPackage()});
        emf.setDataSource(dataSource);

        AbstractJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        vendorAdapter.setShowSql(jpaSettings.isShowSql());
        vendorAdapter.setGenerateDdl(jpaSettings.isGenerateDdl());

        emf.setJpaVendorAdapter(vendorAdapter);
        emf.setJpaPropertyMap(jpaSettings.getProperties());

        return emf;
    }
}