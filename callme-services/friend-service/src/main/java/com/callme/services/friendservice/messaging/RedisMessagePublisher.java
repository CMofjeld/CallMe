package com.callme.services.friendservice.messaging;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RedisMessagePublisher implements MessagePublisher {
    private final RedisTemplate<String, Object> redisTemplate;
    @Override
    public void publish(String topic, Object message) {
        redisTemplate.convertAndSend(topic, message);
    }
}
