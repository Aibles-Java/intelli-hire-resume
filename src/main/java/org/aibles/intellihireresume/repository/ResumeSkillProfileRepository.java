package org.aibles.intellihireresume.repository;

import org.aibles.intellihireresume.entity.ResumeSkillProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ResumeSkillProfileRepository extends JpaRepository<ResumeSkillProfile, String> {
    
    Optional<ResumeSkillProfile> findByResumeId(String resumeId);
    
    void deleteByResumeId(String resumeId);
    
    boolean existsByResumeId(String resumeId);
}