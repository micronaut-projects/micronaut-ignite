package io.micronaut.ignite;

import io.micronaut.ignite.annotation.IgniteRef;
import org.apache.ignite.IgniteCache;

import javax.inject.Singleton;

@Singleton
public class IgniteSampleCache {
    public IgniteCache cache1;
    public IgniteCache cache2;

    public IgniteSampleCache(@IgniteRef(value = "one") IgniteCache cache, @IgniteRef(value = "two") IgniteCache cache1) {
        this.cache1 = cache;
        this.cache2 = cache1;
    }
}
