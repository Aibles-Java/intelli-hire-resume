package org.aibles.intellihireresume.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aibles.intellihireresume.service.RedisJobQueueService;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class RedisJobQueueServiceImpl implements RedisJobQueueService {

    private static final String QUEUE_KEY = "resume:parse:queue";

    private final RedisTemplate<String, String> redisTemplate;

    @Override
    public void enqueue(String resumeId) {
        log.info("Enqueuing resumeId to Redis queue: {}", resumeId);
        redisTemplate.opsForList().leftPush(QUEUE_KEY, resumeId);
        log.info("ResumeId enqueued successfully: {}", resumeId);
    }

    @Override
    public String dequeue() {
        log.info("Dequeuing resumeId from Redis queue");
        return redisTemplate.opsForList().rightPop(QUEUE_KEY);
    }
}
