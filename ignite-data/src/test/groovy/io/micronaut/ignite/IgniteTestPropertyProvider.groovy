package io.micronaut.ignite

import io.micronaut.test.support.TestPropertyProvider

trait IgniteTestPropertyProvider implements TestPropertyProvider{
    @Override
    Map<String, String> getProperties() {
        [
            "datasources.default.name": "mydb",
            "datasources.default.schema-generate": "CREATE_DROP",
            "datasources.default.dialect": "H2",
        ] as Map<String, String>
    }
}
