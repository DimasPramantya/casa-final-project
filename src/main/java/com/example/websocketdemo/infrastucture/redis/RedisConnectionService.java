package com.example.websocketdemo.infrastucture.redis;

import com.example.websocketdemo.presentation.socketio.dto.ConnectionDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class RedisConnectionService {

    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Save a connection record into Redis.
     * @param key the redis key (session_id or user_id)
     */
    public void saveConnection(String key, ConnectionDto dto) {
        if (key == null || key.isBlank()) {
            log.warn("Skip saving to Redis: key is blank");
            return;
        }
        Map<String, Object> payload = new HashMap<>();
        payload.put("queue_name", dto.getQueueName());
        payload.put("machine_id", dto.getMachineId());
        try {
            String json = objectMapper.writeValueAsString(payload);
            stringRedisTemplate.opsForValue().set(key, json);
            // Optional TTL to avoid stale keys if disconnect hook fails; adjust or remove as needed.
            stringRedisTemplate.expire(key, 1, TimeUnit.DAYS);
            log.info("Saved connection to Redis. key={}, value={}", key, json);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize connection payload for key={}: {}", key, e.getMessage());
        } catch (Exception e) {
            log.error("Failed to save connection to Redis for key={}: {}", key, e.getMessage());
        }
    }

    /**
     * Remove a connection record from Redis.
     */
    public void deleteConnection(String key) {
        try {
            if (key != null && !key.isBlank()) {
                stringRedisTemplate.delete(key);
                log.info("Deleted connection from Redis. key={}", key);
            }
        } catch (Exception e) {
            log.error("Failed to delete connection from Redis for key={}: {}", key, e.getMessage());
        }
    }

    private Object tryParseNumber(String maybeNumber) {
        if (maybeNumber == null) return null;
        try {
            // prefer Long; if it doesn't parse, store as String
            return Long.parseLong(maybeNumber);
        } catch (NumberFormatException ignored) {
            return maybeNumber;
        }
    }
}