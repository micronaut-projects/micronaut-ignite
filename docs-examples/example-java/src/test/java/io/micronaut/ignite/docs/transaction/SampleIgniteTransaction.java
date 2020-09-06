package io.micronaut.ignite.docs.transaction;

import io.micronaut.ignite.annotation.IgniteTransaction;
import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;

public class SampleIgniteTransaction {
    private final Ignite ignite;

    SampleIgniteTransaction(Ignite ignite){
        this.ignite = ignite;
    }

    @IgniteTransaction
    public void method() {
        IgniteCache cache = this.ignite.cache("sample");
        cache.put("key", "value");
    }
}
