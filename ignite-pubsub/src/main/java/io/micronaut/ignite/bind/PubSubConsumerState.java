package io.micronaut.ignite.bind;

import org.apache.ignite.IgniteCache;

public class PubSubConsumerState<K,V> {
    private final K key;
    private final V value;
    private final IgniteCache<K,V> cache;

    public PubSubConsumerState(IgniteCache<K, V> cache,  K key, V value) {
        this.key = key;
        this.value = value;
        this.cache = cache;
    }

    public IgniteCache<K,V> getCache() {
        return cache;
    }
}
