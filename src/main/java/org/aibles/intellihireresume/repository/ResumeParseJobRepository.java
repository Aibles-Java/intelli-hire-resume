package org.aibles.intellihireresume.repository;

import org.aibles.intellihireresume.entity.ResumeParseJob;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ResumeParseJobRepository extends JpaRepository<ResumeParseJob, String> {
}