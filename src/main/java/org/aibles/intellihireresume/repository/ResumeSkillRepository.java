package org.aibles.intellihireresume.repository;

import org.aibles.intellihireresume.entity.ResumeSkill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ResumeSkillRepository extends JpaRepository<ResumeSkill, String> {

    List<ResumeSkill> findByResumeId(String resumeId);

    Optional<ResumeSkill> findByResumeIdAndSkillId(String resumeId, String skillId);

    boolean existsByResumeIdAndSkillId(String resumeId, String skillId);

    List<ResumeSkill> findByResumeIdOrderByConfidenceScoreDesc(String resumeId);

    void deleteByResumeId(String resumeId);
}
