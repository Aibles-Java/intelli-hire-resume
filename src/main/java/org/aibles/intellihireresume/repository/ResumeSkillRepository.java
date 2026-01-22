package org.aibles.intellihireresume.repository;

import org.aibles.intellihireresume.entity.ResumeSkill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ResumeSkillRepository extends JpaRepository<ResumeSkill, String> {
}