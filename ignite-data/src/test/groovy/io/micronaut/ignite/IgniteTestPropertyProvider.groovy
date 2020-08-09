package io.micronaut.ignite

import io.micronaut.test.support.TestPropertyProvider

trait IgniteTestPropertyProvider implements TestPropertyProvider{
    @Override
    Map<String, String> getProperties() {
        [
            "ignite.enabled"             : true,
            "ignite.clients.default.path": "classpath:ignite_data.cfg",
            "ignite.datasources.default.cache": "mydb",
            "ignite.datasources.default.schema-generate": "CREATE_DROP"
        ] as Map<String, String>
    }
}
