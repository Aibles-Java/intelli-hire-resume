package org.aibles.intellihireresume.repository;

import org.aibles.intellihireresume.entity.ResumeEducation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ResumeEducationRepository extends JpaRepository<ResumeEducation, String> {

    List<ResumeEducation> findByResumeId(String resumeId);

    void deleteByResumeId(String resumeId);
}