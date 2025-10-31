package com.ourvoiceourrights.service.cache;

import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnExpression("'${CACHE_PROVIDER:${app.cache.provider-env}}'.equalsIgnoreCase('redis')")
public class RedisCacheService implements CacheService {

    private final RedisTemplate<String, Object> redisTemplate;

    public RedisCacheService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public <T> Optional<T> get(String cacheName, Object key, Class<T> type) {
        Object raw = redisTemplate.opsForValue().get(buildKey(cacheName, key));
        if (raw == null || !type.isInstance(raw)) {
            return Optional.empty();
        }
        return Optional.of(type.cast(raw));
    }

    @Override
    public <E> Optional<java.util.List<E>> getList(String cacheName, Object key, Class<E> elementType) {
        Object raw = redisTemplate.opsForValue().get(buildKey(cacheName, key));
        if (!(raw instanceof java.util.List<?> list)) {
            return Optional.empty();
        }
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
        String composedKey = buildKey(cacheName, key);
        redisTemplate.opsForValue().set(composedKey, value, ttl.toMillis(), TimeUnit.MILLISECONDS);
    }

    @Override
    public void evict(String cacheName, Object key) {
        redisTemplate.delete(buildKey(cacheName, key));
    }

    @Override
    public void clear(String cacheName) {
        // Use pattern delete to avoid accidental flush
        String pattern = cacheName + "::*";
        var keys = redisTemplate.keys(pattern);
        if (keys != null) {
            keys.forEach(redisTemplate::delete);
        }
    }

    private String buildKey(String cacheName, Object key) {
        return cacheName + "::" + (key == null ? "__null" : key.hashCode());
    }
}
