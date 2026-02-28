package org.aibles.intellihireresume.service;

public interface RedisJobQueueService {

    void enqueue(String resumeId);

    String dequeue();
}
