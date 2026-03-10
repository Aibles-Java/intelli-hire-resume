package org.aibles.intellihireresume.service;

import org.aibles.intellihireresume.dto.ParseJobResponse;
import org.aibles.intellihireresume.entity.enums.JobStatus;
import org.aibles.intellihireresume.entity.enums.JobType;

public interface ResumeParseJobService {

    ParseJobResponse create(String resumeId, JobType jobType);

    ParseJobResponse getById(String id);

    ParseJobResponse cancel(String id);

    void updateStatus(String id, JobStatus status);

    ParseJobResponse createOrReset(String resumeId, JobType jobType);
}
