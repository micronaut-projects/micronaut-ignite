package io.micronaut.ignite;

import edu.umd.cs.findbugs.annotations.Nullable;
import io.micronaut.data.exceptions.DataAccessException;
import io.micronaut.data.runtime.mapper.ResultReader;
import org.apache.ignite.cache.query.FieldsQueryCursor;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class CursorResultReader implements ResultReader<CursorResultReader.ResultCursorWrapper, String> {
    @Nullable
    @Override
    public <T> T getRequiredValue(ResultCursorWrapper resultSet, String name, Class<T> type) throws DataAccessException {
        return resultSet.get(name);
    }

    @Override
    public boolean next(ResultCursorWrapper resultSet) {
        return resultSet.next();
    }

    public static class ResultCursorWrapper {
        private final FieldsQueryCursor<List<?>> query;
        private final Iterator<List<?>> iterator;
        private List<?> current;
        private Map<String, Integer> indexLookup = new HashMap<>();

        public ResultCursorWrapper(FieldsQueryCursor<List<?>> query) {
            this.query = query;
            for (int x = 0; x < query.getColumnsCount(); x++) {
                indexLookup.put(query.getFieldName(x), x);
            }
            this.iterator = query.iterator();
        }

        public boolean next() {
            if(this.iterator.hasNext()){
                current = this.iterator.next();
                return true;
            }
            return false;
        }

        public <T> T get(String name) {
            return (T) current.get(indexLookup.get(name));
        }

        public <T> T get(int index) {
            return (T) current.get(index);
        }

        public boolean hasNext() {
            return this.iterator.hasNext();
        }
    }
//
//    private Iterator<List<?>> iterator;
//    private Map<String, Integer> indexLookup = new HashMap<>();
//
//    public CursorResultReader() {
////        this.iterator = iterator;
////        for (int x = 0; x < cursor.getColumnsCount(); x++) {
////            indexLookup.put(cursor.getFieldName(x), x);
////        }
//    }
//
//    @Nullable
//    @Override
//    public <T> T getRequiredValue(List<?> resultSet, String name, Class<T> type) throws DataAccessException {
//        return (T) resultSet.get(indexLookup.get(name));
//    }
//
//    @Override
//    public boolean next(List<?> resultSet) {
//
//        return iterator.hasNext();
//    }
}
