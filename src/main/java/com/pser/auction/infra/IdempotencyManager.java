package com.pser.auction.infra;

import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class IdempotencyManager {
    private final RedisTemplate<String, String> redisTemplate;

    public boolean isMarked(String key) {
        String value = redisTemplate.opsForValue().get(key);
        return value != null && value.equals("cached");
    }

    synchronized public boolean mark(String key) {
        if (isMarked(key)) {
            return false;
        }
        redisTemplate.opsForValue().set(key, "cached", 1, TimeUnit.DAYS);
        return true;
    }

    public void unmark(String key) {
        redisTemplate.delete(key);
    }
}
