package io.micronaut.ignite.docs.transaction

import io.micronaut.ignite.annotation.IgniteTransaction
import org.apache.ignite.Ignite
import org.apache.ignite.IgniteCache

class SampleIgniteTransaction constructor(private val ignite: Ignite) {

    @IgniteTransaction
    fun method() {
        val cache: IgniteCache<String, String> = ignite.cache("sample")
        cache.put("key", "value")
    }
}
