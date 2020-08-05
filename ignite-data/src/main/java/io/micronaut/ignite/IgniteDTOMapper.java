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
package io.micronaut.ignite;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import io.micronaut.data.exceptions.DataAccessException;
import io.micronaut.data.model.DataType;
import io.micronaut.data.model.runtime.RuntimePersistentEntity;
import io.micronaut.data.model.runtime.RuntimePersistentProperty;
import io.micronaut.data.runtime.mapper.BeanIntrospectionMapper;
import io.micronaut.http.codec.MediaTypeCodec;
import org.apache.ignite.cache.query.FieldsQueryCursor;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class IgniteDTOMapper<T, R> implements BeanIntrospectionMapper<List<?>, R> {
    FieldsQueryCursor<List<?>> cursor;
    private final RuntimePersistentEntity<T> persistentEntity;
    private final ConcurrentHashMap<String, Integer> nameLookup = new ConcurrentHashMap<>(10);
    private final @Nullable
    MediaTypeCodec jsonCodec;

    public IgniteDTOMapper(
        RuntimePersistentEntity<T> persistentEntity,
        FieldsQueryCursor<List<?>> cursor,
        @Nullable MediaTypeCodec jsonCodec) {
        this.persistentEntity = persistentEntity;
        this.cursor = cursor;
        this.jsonCodec = jsonCodec;
        for (int x = 0; x < cursor.getColumnsCount(); x++) {
            nameLookup.put(cursor.getFieldName(x), x);
        }
    }

    @Nullable
    @Override
    public Object read(@NonNull List<?> object, @NonNull String name) {
        RuntimePersistentProperty<T> pp = persistentEntity.getPropertyByName(name);
        if (pp == null) {
            throw new DataAccessException("DTO projection defines a property [" + name + "] that doesn't exist on root entity: " + persistentEntity.getName());
        } else {
            return read(object, pp);
        }

    }

    private <T> T read(List<?> object, String field, DataType dataType) {
        if (nameLookup.containsKey(field)) {
            Integer idx = nameLookup.get(field);
            return (T) object.get(idx);
        }
        throw new DataAccessException("DTO failed to resolve field for property [" + field + "]");
    }

    private Object read(List<?> object, RuntimePersistentProperty<T> property) {
        String propertyName = property.getPersistedName();
        DataType dataType = property.getDataType();
        if (dataType == DataType.JSON) {
            String data = read(object, propertyName, DataType.STRING);
            return jsonCodec.decode(property.getArgument(), data);
        } else {
            return read(object, propertyName, dataType);
        }
    }

}
