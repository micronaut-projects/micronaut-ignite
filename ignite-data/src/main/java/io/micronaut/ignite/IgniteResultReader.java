package io.micronaut.ignite;

import edu.umd.cs.findbugs.annotations.Nullable;
import io.micronaut.data.runtime.mapper.ResultReader;
import org.apache.ignite.cache.query.FieldsQueryCursor;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class IgniteResultReader implements ResultReader<List<?>, String > {

    private final FieldsQueryCursor<List<?>> cursor;
    private final Iterator<List<?>> iterator;
    private final Map<String, Integer> fieldMapping = new HashMap<>();
    public IgniteResultReader(FieldsQueryCursor<List<?>> cursor) {
        this.cursor = cursor;
        for( int x = 0 ; x < cursor.getColumnsCount(); x++) {
            fieldMapping.put(cursor.getFieldName(x), x);
        }

        this.iterator = cursor.iterator();
    }

    @Nullable
    @Override
    public <T> T getRequiredValue(List<?> resultSet, String name, Class<T> type) {

        return null;
    }

    @Override
    public boolean next(List<?> resultSet) {
        return iterator.hasNext();
    }
}
