package com.ourvoiceourrights.service.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnExpression("'${CACHE_PROVIDER:${app.cache.provider-env}}'.equalsIgnoreCase('caffeine')")
public class CaffeineCacheService implements CacheService {

    private final ConcurrentMap<String, CacheHolder> caches = new ConcurrentHashMap<>();

    @Override
    public <T> Optional<T> get(String cacheName, Object key, Class<T> type) {
        CacheHolder holder = caches.get(cacheName);
        if (holder == null) {
            return Optional.empty();
        }
        Object value = holder.cache.getIfPresent(key);
        if (value == null) {
            return Optional.empty();
        }
        return type.isInstance(value) ? Optional.of(type.cast(value)) : Optional.empty();
    }

    @Override
    public <E> Optional<java.util.List<E>> getList(String cacheName, Object key, Class<E> elementType) {
        CacheHolder holder = caches.get(cacheName);
        if (holder == null) {
            return Optional.empty();
        }
        Object value = holder.cache.getIfPresent(key);
        if (!(value instanceof java.util.List<?> list)) {
            return Optional.empty();
        }
        // verify element types
        for (Object o : list) {
            if (o != null && !elementType.isInstance(o)) {
                return Optional.empty();
            }
        }
        @SuppressWarnings("unchecked")
        java.util.List<E> casted = (java.util.List<E>) list;
        return Optional.of(casted);
    }

    @Override
    public void put(String cacheName, Object key, Object value, Duration ttl) {
        CacheHolder holder = caches.compute(cacheName, (name, existing) -> {
            if (existing == null || !existing.ttl.equals(ttl)) {
                Cache<Object, Object> cache = Caffeine.newBuilder()
                        .expireAfterWrite(ttl)
                        .maximumSize(10_000)
                        .recordStats()
                        .build();
                return new CacheHolder(cache, ttl);
            }
            return existing;
        });
        holder.cache.put(key, value);
    }

    @Override
    public void evict(String cacheName, Object key) {
        CacheHolder holder = caches.get(cacheName);
        if (holder != null) {
            holder.cache.invalidate(key);
        }
    }

    @Override
    public void clear(String cacheName) {
        CacheHolder holder = caches.get(cacheName);
        if (holder != null) {
            holder.cache.invalidateAll();
        }
    }

    private record CacheHolder(Cache<Object, Object> cache, Duration ttl) {
    }
}
