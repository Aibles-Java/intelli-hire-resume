package org.aibles.intellihireresume.repository;

import org.aibles.intellihireresume.entity.ResumeParseJob;
import org.aibles.intellihireresume.entity.enums.JobStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ResumeParseJobRepository extends JpaRepository<ResumeParseJob, String> {

    Optional<ResumeParseJob> findByResumeId(String resumeId);

    List<ResumeParseJob> findByStatus(JobStatus status);
}
