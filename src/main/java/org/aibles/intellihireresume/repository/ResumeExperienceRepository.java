package org.aibles.intellihireresume.repository;

import org.aibles.intellihireresume.entity.ResumeExperience;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ResumeExperienceRepository extends JpaRepository<ResumeExperience, String> {

    List<ResumeExperience> findByResumeId(String resumeId);
}