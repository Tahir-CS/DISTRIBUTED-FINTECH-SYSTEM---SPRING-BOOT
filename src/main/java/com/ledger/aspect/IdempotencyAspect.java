package com.ledger.aspect;

import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.concurrent.TimeUnit;

@Aspect
@Component
public class IdempotencyAspect {

    private final RedisTemplate<String, Object> redisTemplate;

    public IdempotencyAspect(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Around("@annotation(com.ledger.annotation.Idempotent)")
    public Object enforceIdempotency(ProceedingJoinPoint joinPoint) throws Throwable {
       
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return joinPoint.proceed(); // If not an HTTP request, just proceed
        }
        HttpServletRequest request = attributes.getRequest();

        // extracting the Idempotency Key Header
        String idempotencyKey = request.getHeader("Idempotency-Key");
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            throw new IllegalArgumentException("Idempotency-Key header is absolutely required for this endpoint!");
        }

        // checking Redis if we have already processed this exact request
        Object cachedResponse = redisTemplate.opsForValue().get("idempotency:" + idempotencyKey);
        
        if (cachedResponse != null) {
            System.out.println("⚠️ IDEMPOTENCY HIT! Intercepted duplicate request for key: " + idempotencyKey);
            System.out.println("⚠️ Returning cached response instantly from Redis. The database was NOT touched.");
            // instantly return the cached response without running the transfer logic!
            return cachedResponse;
        }

        // if it is a new request, we proceed with executing the actual method logic
        Object result = joinPoint.proceed();
        
        // Cache the successful response in Redis with a 24-hour expiration
        redisTemplate.opsForValue().set("idempotency:" + idempotencyKey, result, 24, TimeUnit.HOURS);

        return result;
    }
}
