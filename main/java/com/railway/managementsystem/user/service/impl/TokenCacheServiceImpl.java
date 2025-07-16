package com.railway.managementsystem.user.service.impl;

import com.railway.managementsystem.user.service.TokenCacheService;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class TokenCacheServiceImpl implements TokenCacheService {
    private final RedisTemplate<String, String> redisTemplate;

    // Constructor injection for RedisTemplate
    public TokenCacheServiceImpl(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public String generateAndCacheToken(String username) {
        String token = UUID.randomUUID().toString(); // Generate a unique token
        String key = "token:" + token;          // Use "token:" + token as the key

        // Store the username associated with the token in Redis, with a TTL
        redisTemplate.opsForValue().set(key, username, 1, TimeUnit.HOURS);
        return token;
    }

    @Override
    public String getUsernameByToken(String token) {
        String key = "token:" + token;
        return redisTemplate.opsForValue().get(key);
    }

    @Override
    public void invalidateToken(String token) {
        String key = "token:" + token;
        redisTemplate.delete(key);
    }

    @Override
    public void refreshTokenExpiry(String token) {
        String key = "token:" + token;
        if (redisTemplate.hasKey(key)) {
            // Reset the TTL to 1 hour.
            redisTemplate.expire(key, 1, TimeUnit.HOURS);
        }
    }
}