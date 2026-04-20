package org.aibles.intellihireresume.repository;

import org.aibles.intellihireresume.entity.ResumeContact;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ResumeContactRepository extends JpaRepository<ResumeContact, String> {

    Optional<ResumeContact> findByResumeId(String resumeId);

    void deleteByResumeId(String resumeId);
}
