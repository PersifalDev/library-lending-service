package ru.haritonenko.librarylendingservice.lendings.domain.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import ru.haritonenko.librarylendingservice.config.properties.CacheProperties;
import ru.haritonenko.librarylendingservice.lendings.domain.Lending;

import java.util.Set;

@Slf4j
@Component
public class LendingCacheService {

    private final RedisTemplate<String, Lending> redisLendingTemplate;
    private final StringRedisTemplate stringRedisTemplate;
    private final CacheProperties cacheProperties;

    public LendingCacheService(
            ObjectProvider<RedisTemplate<String, Lending>> redisLendingTemplateProvider,
            ObjectProvider<StringRedisTemplate> stringRedisTemplateProvider,
            CacheProperties cacheProperties
    ) {
        this.redisLendingTemplate = redisLendingTemplateProvider.getIfAvailable();
        this.stringRedisTemplate = stringRedisTemplateProvider.getIfAvailable();
        this.cacheProperties = cacheProperties;
    }

    public Lending getLending(Long id) {
        if (redisLendingTemplate == null || id == null) {
            return null;
        }
        String key = getLendingCacheKey(id);
        try {
            return redisLendingTemplate.opsForValue().get(key);
        } catch (RedisConnectionFailureException ex) {
            log.warn("Redis unavailable during lending cache read, fallback to DB. key={}", key, ex);
            return null;
        }
    }

    public void cacheLending(Lending lending) {
        if (redisLendingTemplate == null || lending == null || lending.getId() == null) {
            return;
        }
        try {
            redisLendingTemplate.opsForValue().set(
                    getLendingCacheKey(lending.getId()),
                    lending,
                    cacheProperties.getLendingsTtl()
            );
            addLendingToIndex(lending);
        } catch (RedisConnectionFailureException ex) {
            log.warn("Redis unavailable during lending cache write. lendingId={}", lending.getId(), ex);
        }
    }

    public void invalidateLending(Long id) {
        if (redisLendingTemplate == null || id == null) {
            return;
        }
        try {
            redisLendingTemplate.delete(getLendingCacheKey(id));
        } catch (RedisConnectionFailureException ex) {
            log.warn("Redis unavailable during lending cache invalidation. lendingId={}", id, ex);
        }
    }

    public void invalidateByClientId(Long clientId) {
        invalidateByIndex(getLendingByClientCacheKey(clientId));
    }

    public void invalidateByBookId(Long bookId) {
        invalidateByIndex(getLendingByBookCacheKey(bookId));
    }

    private void addLendingToIndex(Lending lending) {
        if (stringRedisTemplate == null || lending.getClient() == null || lending.getBook() == null) {
            return;
        }
        String lendingId = String.valueOf(lending.getId());
        stringRedisTemplate.opsForSet().add(getLendingByClientCacheKey(lending.getClient().getId()), lendingId);
        stringRedisTemplate.opsForSet().add(getLendingByBookCacheKey(lending.getBook().getId()), lendingId);
        stringRedisTemplate.expire(getLendingByClientCacheKey(lending.getClient().getId()), cacheProperties.getLendingsTtl());
        stringRedisTemplate.expire(getLendingByBookCacheKey(lending.getBook().getId()), cacheProperties.getLendingsTtl());
    }

    private void invalidateByIndex(String indexKey) {
        if (redisLendingTemplate == null || stringRedisTemplate == null || indexKey == null) {
            return;
        }
        try {
            Set<String> lendingIds = stringRedisTemplate.opsForSet().members(indexKey);
            if (lendingIds != null) {
                for (String lendingId : lendingIds) {
                    redisLendingTemplate.delete(getLendingCacheKey(Long.valueOf(lendingId)));
                }
            }
            stringRedisTemplate.delete(indexKey);
        } catch (RedisConnectionFailureException ex) {
            log.warn("Redis unavailable during indexed lending cache invalidation. indexKey={}", indexKey, ex);
        }
    }

    private String getLendingCacheKey(Long id) {
        return cacheProperties.getLendingKeyPrefix() + id;
    }

    private String getLendingByClientCacheKey(Long clientId) {
        return clientId == null ? null : cacheProperties.getLendingByClientKeyPrefix() + clientId;
    }

    private String getLendingByBookCacheKey(Long bookId) {
        return bookId == null ? null : cacheProperties.getLendingByBookKeyPrefix() + bookId;
    }
}
