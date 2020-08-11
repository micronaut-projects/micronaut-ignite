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
import io.micronaut.context.Qualifier;
import io.micronaut.context.event.ApplicationEventListener;
import io.micronaut.core.annotation.AnnotationMetadata;
import io.micronaut.core.beans.BeanIntrospection;
import io.micronaut.core.beans.BeanIntrospector;
import io.micronaut.core.util.ArrayUtils;
import io.micronaut.core.util.CollectionUtils;
import io.micronaut.data.annotation.MappedEntity;
import io.micronaut.data.annotation.Repository;
import io.micronaut.data.annotation.sql.SqlMembers;
import io.micronaut.data.model.PersistentEntity;
import io.micronaut.data.runtime.config.DataSettings;
import io.micronaut.data.runtime.config.SchemaGenerate;
import io.micronaut.ignite.IgniteSqlQueryBuilder;
import io.micronaut.ignite.event.IgniteStartEvent;
import io.micronaut.inject.qualifiers.Qualifiers;
import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.cache.query.SqlFieldsQuery;

import javax.inject.Singleton;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Singleton
public class SchemaGenerator implements ApplicationEventListener<IgniteStartEvent> {
    private final IgniteDataConfiguration configuration;
    private final BeanContext beanContext;

    public SchemaGenerator(IgniteDataConfiguration configuration, BeanContext beanContext) {
        this.configuration = configuration;
        this.beanContext = beanContext;
    }


    @Override
    public void onApplicationEvent(IgniteStartEvent event) {

        Ignite ignite = event.getInstance();
        Qualifier qualifier = event.getQualifier().orElse(Qualifiers.byName("default"));

        SchemaGenerate schemaGenerate = configuration.getSchemaGenerate();
        if (schemaGenerate != null && schemaGenerate != SchemaGenerate.NONE) {
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
            IgniteSqlQueryBuilder builder = new IgniteSqlQueryBuilder();
            if (ArrayUtils.isNotEmpty(entities)) {
                switch (schemaGenerate) {
                    case CREATE_DROP:
                        for (PersistentEntity entity : entities) {
                            AnnotationMetadata metadata = entity.getAnnotationMetadata();
                            Optional<String> schema = metadata.stringValue(MappedEntity.class, SqlMembers.SCHEMA);
                            String repository = metadata.stringValue(Repository.class).orElse("default");
                            if (!qualifier.contains(Qualifiers.byName(repository)) || !schema.isPresent())
                                continue;
                            IgniteCache cache = ignite.cache(schema.get());
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
                            AnnotationMetadata metadata = entity.getAnnotationMetadata();
                            Optional<String> schema = metadata.stringValue(MappedEntity.class, SqlMembers.SCHEMA);
                            String repository = metadata.stringValue(Repository.class).orElse("default");
                            if (!qualifier.contains(Qualifiers.byName(repository)) || !schema.isPresent())
                                continue;
                            IgniteCache cache = ignite.cache(schema.get());

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


//                switch (schemaGenerate) {
//                    case CREATE_DROP: {
//                        try {
//                            String sql = builder.buildBatchDropTableStatement(entities);
//                            if (DataSettings.QUERY_LOG.isDebugEnabled()) {
//                                DataSettings.QUERY_LOG.debug("Dropping Table: \n{}", sql);
//                            }
//                            SqlFieldsQuery query = new SqlFieldsQuery(sql);
//                            cache.query(query);
//                        } catch (Exception e) {
//                            if (DataSettings.QUERY_LOG.isTraceEnabled()) {
//                                DataSettings.QUERY_LOG.trace("Drop Failed: " + e.getMessage());
//                            }
//                        }
//                    }
//                    case CREATE: {
//                        String sql = builder.buildBatchCreateTableStatement(entities);
//                        if (DataSettings.QUERY_LOG.isDebugEnabled()) {
//                            DataSettings.QUERY_LOG.debug("Dropping Table: \n{}", sql);
//                        }
//                        SqlFieldsQuery query = new SqlFieldsQuery(sql);
//                        cache.query(query);
//                    }
//                    break;
//                    default:
//                        // do nothing
//                }
            }
        }


//        if (schemaGenerate != null && schemaGenerate != SchemaGenerate.NONE) {
//            String name = configuration.getName();
//            String client = configuration.getClient();
//
//            if (ArrayUtils.isNotEmpty(entities)) {
//                IgniteSqlQueryBuilder builder = new IgniteSqlQueryBuilder();
//                IgniteCache cache = ignite.cache(configuration.getCache());
//                if (configuration.isBatchGenerate()) {
//
//                } else {
//
//                }
//            }
//        }

    }
}
