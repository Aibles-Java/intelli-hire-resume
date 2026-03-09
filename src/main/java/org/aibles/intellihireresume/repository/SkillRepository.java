package org.aibles.intellihireresume.repository;

import org.aibles.intellihireresume.entity.Skill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SkillRepository extends JpaRepository<Skill, String> {

    List<Skill> findByIsActiveTrue();

    List<Skill> findByCategoryAndIsActiveTrue(String category);

    List<Skill> findByNameContainingIgnoreCaseAndIsActiveTrue(String name);
}
