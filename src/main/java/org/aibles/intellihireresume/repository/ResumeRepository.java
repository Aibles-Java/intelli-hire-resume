package org.aibles.intellihireresume.repository;

import org.aibles.intellihireresume.entity.Resume;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ResumeRepository extends JpaRepository<Resume, String> {
}