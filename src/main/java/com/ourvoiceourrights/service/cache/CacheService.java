package com.ourvoiceourrights.service.cache;

import java.time.Duration;
import java.util.Optional;

public interface CacheService {

    <T> Optional<T> get(String cacheName, Object key, Class<T> type);

    /**
     * Retrieve a cached list and ensure elements are of the requested element type.
     * This helps avoid unchecked casts at call sites.
     */
    <E> Optional<java.util.List<E>> getList(String cacheName, Object key, Class<E> elementType);

    void put(String cacheName, Object key, Object value, Duration ttl);

    void evict(String cacheName, Object key);

    void clear(String cacheName);
}
