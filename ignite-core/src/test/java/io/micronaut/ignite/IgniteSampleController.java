package io.micronaut.ignite;

import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.ignite.annotation.IgniteCacheRef;
import org.apache.ignite.IgniteCache;

import javax.inject.Inject;

@Controller("/ignite")
public class IgniteSampleController {
    @Inject
    @IgniteCacheRef("t1")
    IgniteCache<String, String> cache;

    @Get("/{key}/{value}")
    String cache(String key, String value) {
        cache.put(key, value);
        return value;
    }

    @Get("/{key}")
    String key(String key) {
        return cache.get(key);
    }
}
