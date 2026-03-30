package org.aibles.intellihireresume.repository;

import org.aibles.intellihireresume.entity.ResumeReview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ResumeReviewRepository extends JpaRepository<ResumeReview, String> {

    Optional<ResumeReview> findByResumeId(String resumeId);
}
