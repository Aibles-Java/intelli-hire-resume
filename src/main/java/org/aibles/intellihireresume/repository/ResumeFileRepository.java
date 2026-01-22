package org.aibles.intellihireresume.repository;

import org.aibles.intellihireresume.entity.ResumeFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ResumeFileRepository extends JpaRepository<ResumeFile, String> {
}