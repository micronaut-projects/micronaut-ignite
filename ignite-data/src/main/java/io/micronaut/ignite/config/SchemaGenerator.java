/*
 * Copyright 2017-2020 original authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.micronaut.ignite.config;

import io.micronaut.context.BeanContext;
import io.micronaut.context.BeanLocator;
import io.micronaut.context.BeanRegistration;
import io.micronaut.context.Qualifier;
import io.micronaut.context.event.ApplicationEventListener;
import io.micronaut.core.beans.BeanIntrospection;
import io.micronaut.core.beans.BeanIntrospector;
import io.micronaut.core.util.ArrayUtils;
import io.micronaut.core.util.CollectionUtils;
import io.micronaut.data.annotation.MappedEntity;
import io.micronaut.data.model.PersistentEntity;
import io.micronaut.data.model.query.builder.sql.Dialect;
import io.micronaut.data.model.query.builder.sql.SqlQueryBuilder;
import io.micronaut.data.runtime.config.DataSettings;
import io.micronaut.data.runtime.config.SchemaGenerate;
import io.micronaut.ignite.event.IgniteStartEvent;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.qualifiers.Qualifiers;
import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.cache.query.SqlFieldsQuery;

import javax.inject.Singleton;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Singleton
public class SchemaGenerator implements ApplicationEventListener<IgniteStartEvent> {
    private final List<IgniteDataConfiguration> configurations;
    private final BeanContext beanContext;

    public SchemaGenerator(List<IgniteDataConfiguration> configurations, BeanContext beanContext) {
        this.configurations = configurations;
        this.beanContext = beanContext;
    }


    @Override
    public void onApplicationEvent(IgniteStartEvent event) {

        Ignite ignite = event.getInstance();
        Qualifier qualifier = event.getQualifier().orElse(Qualifiers.byName("default"));

        for (IgniteDataConfiguration configuration : configurations) {
            if (!qualifier.contains(Qualifiers.byName(configuration.getClient())))
                continue;

            SchemaGenerate schemaGenerate = configuration.getSchemaGenerate();
            if (schemaGenerate != null && schemaGenerate != SchemaGenerate.NONE) {
                String name = configuration.getName();
                String client = configuration.getClient();
                List<String> packages = configuration.getPackages();

                Collection<BeanIntrospection<Object>> introspections;
                if (CollectionUtils.isNotEmpty(packages)) {
                    introspections = BeanIntrospector.SHARED.findIntrospections(MappedEntity.class, packages.toArray(new String[0]));
                } else {
                    introspections = BeanIntrospector.SHARED.findIntrospections(MappedEntity.class);
                }
                PersistentEntity[] entities = introspections.stream()
                    // filter out inner / internal / abstract(MappedSuperClass) classes
                    .filter(i -> !i.getBeanType().getName().contains("$"))
                    .filter(i -> !java.lang.reflect.Modifier.isAbstract(i.getBeanType().getModifiers()))
                    .map(PersistentEntity::of).toArray(PersistentEntity[]::new);
                if (ArrayUtils.isNotEmpty(entities)) {
                    SqlQueryBuilder builder = new SqlQueryBuilder(Dialect.H2);
                    IgniteCache cache = ignite.cache(configuration.getCache());
                    if (configuration.isBatchGenerate()) {
                        switch (schemaGenerate) {
                            case CREATE_DROP: {
                                try {
                                    String sql = builder.buildBatchDropTableStatement(entities);
                                    if (DataSettings.QUERY_LOG.isDebugEnabled()) {
                                        DataSettings.QUERY_LOG.debug("Dropping Table: \n{}", sql);
                                    }
                                    SqlFieldsQuery query = new SqlFieldsQuery(sql);
                                    cache.query(query);
                                } catch (Exception e) {
                                    if (DataSettings.QUERY_LOG.isTraceEnabled()) {
                                        DataSettings.QUERY_LOG.trace("Drop Failed: " + e.getMessage());
                                    }
                                }
                            }
                            case CREATE: {
                                String sql = builder.buildBatchCreateTableStatement(entities);
                                if (DataSettings.QUERY_LOG.isDebugEnabled()) {
                                    DataSettings.QUERY_LOG.debug("Dropping Table: \n{}", sql);
                                }
                                SqlFieldsQuery query = new SqlFieldsQuery(sql);
                                cache.query(query);
                            }
                            break;
                            default:
                                // do nothing
                        }
                    } else {
                        switch (schemaGenerate) {
                            case CREATE_DROP:
                                for (PersistentEntity entity : entities) {
                                    try {
                                        String[] statements = builder.buildDropTableStatements(entity);
                                        for (String sql : statements) {
                                            if (DataSettings.QUERY_LOG.isDebugEnabled()) {
                                                DataSettings.QUERY_LOG.debug("Dropping Table: \n{}", sql);
                                            }
                                            SqlFieldsQuery query = new SqlFieldsQuery(sql);
                                            cache.query(query);
                                        }
                                    } catch (Exception e) {
                                        if (DataSettings.QUERY_LOG.isTraceEnabled()) {
                                            DataSettings.QUERY_LOG.trace("Drop Failed: " + e.getMessage());
                                        }
                                    }
                                }
                            case CREATE:
                                for (PersistentEntity entity : entities) {
                                    String[] statements = builder.buildCreateTableStatements(entity);
                                    for (String sql : statements) {
                                        if (DataSettings.QUERY_LOG.isDebugEnabled()) {
                                            DataSettings.QUERY_LOG.debug("Executing CREATE statement: \n{}", sql);
                                        }

                                        try {
                                            SqlFieldsQuery query = new SqlFieldsQuery(sql);
                                            cache.query(query);
                                        } catch (Exception e) {
                                            if (DataSettings.QUERY_LOG.isWarnEnabled()) {
                                                DataSettings.QUERY_LOG.warn("CREATE Statement Failed: " + e.getMessage());
                                            }
                                        }
                                    }
                                }
                                break;
                            default:
                                // do nothing
                        }
                    }
                }
            }
        }
    }
}
