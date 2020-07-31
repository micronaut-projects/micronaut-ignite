package io.micronaut.ignite;

import io.micronaut.ignite.annotation.IgniteCacheRef;
import org.apache.ignite.IgniteCache;

import javax.inject.Singleton;

@Singleton
public class IgniteSampleCache {
    public IgniteCache cache1;

    public IgniteSampleCache(@IgniteCacheRef(value = "test") IgniteCache cache) {
        this.cache1 = cache;
    }
}
