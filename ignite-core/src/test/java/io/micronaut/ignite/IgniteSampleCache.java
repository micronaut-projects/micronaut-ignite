package io.micronaut.ignite;

import io.micronaut.ignite.annotation.IgniteCacheRef;
import org.apache.ignite.IgniteCache;

import javax.inject.Singleton;

@Singleton
public class IgniteSampleCache {
    public IgniteCache cache1;
    public IgniteCache cache2;

    public IgniteSampleCache(@IgniteCacheRef(value = "test") IgniteCache cache,@IgniteCacheRef(value = "test1") IgniteCache cache1) {
        this.cache1 = cache;
        this.cache2 = cache1;
    }
}
