package org.aibles.intellihireresume.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ResumeQueueScheduler {

    private final RedisJobQueueService redisJobQueueService;
    private final ResumeWorkerService resumeWorkerService;

    @Scheduled(fixedDelay = 5000)
    public void pollAndDispatch() {
        try {
            String resumeId = redisJobQueueService.dequeue();
            if (resumeId != null) {
                log.info("Dispatching worker for resumeId={}", resumeId);
                resumeWorkerService.processJob(resumeId);
            }
        } catch (Exception e) {
            log.error("Error polling Redis queue: {}", e.getMessage());
        }
    }
}
