package org.aibles.intellihireresume.repository;

import org.aibles.intellihireresume.entity.ResumeExperience;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ResumeExperienceRepository extends JpaRepository<ResumeExperience, String> {
}