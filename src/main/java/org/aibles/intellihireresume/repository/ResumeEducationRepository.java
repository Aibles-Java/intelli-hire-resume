package org.aibles.intellihireresume.repository;

import org.aibles.intellihireresume.entity.ResumeEducation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ResumeEducationRepository extends JpaRepository<ResumeEducation, String> {
}