package ru.haritonenko.librarylendingservice.config.ratelimit;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import ru.haritonenko.librarylendingservice.config.properties.RateLimitProperties;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class RateLimitInterceptor implements HandlerInterceptor {

    private final StringRedisTemplate stringRedisTemplate;
    private final RateLimitProperties rateLimitProperties;

    public RateLimitInterceptor(
            ObjectProvider<StringRedisTemplate> stringRedisTemplateProvider,
            RateLimitProperties rateLimitProperties
    ) {
        this.stringRedisTemplate = stringRedisTemplateProvider.getIfAvailable();
        this.rateLimitProperties = rateLimitProperties;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        if (!rateLimitProperties.isEnabled() || stringRedisTemplate == null) {
            return true;
        }

        String key = rateLimitProperties.getKeyPrefix() + clientIp(request);
        try {
            Long current = stringRedisTemplate.opsForValue().increment(key);
            if (current != null && current == 1L) {
                Duration window = rateLimitProperties.getWindow();
                stringRedisTemplate.expire(key, window.toMillis(), TimeUnit.MILLISECONDS);
            }

            long remaining = Math.max(0L, rateLimitProperties.getCapacity() - (current == null ? 0L : current));
            response.setHeader("X-RateLimit-Limit", String.valueOf(rateLimitProperties.getCapacity()));
            response.setHeader("X-RateLimit-Remaining", String.valueOf(remaining));

            if (current != null && current > rateLimitProperties.getCapacity()) {
                response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                response.setCharacterEncoding("UTF-8");
                response.setContentType("application/json");
                response.setHeader("Retry-After", String.valueOf(rateLimitProperties.getWindow().getSeconds()));
                response.getWriter().write(
                        "{\"message\":\"Rate limit exceeded\",\"detailedMessage\":\"Too many requests\",\"timestamp\":\""
                                + LocalDateTime.now()
                                + "\"}"
                );
                return false;
            }
        } catch (DataAccessException ex) {
            log.warn("Redis unavailable during rate limit check, request allowed", ex);
        }

        return true;
    }

    private String clientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.trim().isEmpty()) {
            return forwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
