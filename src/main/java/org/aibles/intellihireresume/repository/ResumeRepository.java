package org.aibles.intellihireresume.repository;

import org.aibles.intellihireresume.entity.Resume;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ResumeRepository extends JpaRepository<Resume, String> {
    
    @Query("SELECT r FROM Resume r WHERE r.id = :id AND r.isActive = true")
    Optional<Resume> findByIdActive(@Param("id") String id);
    
    @Query("SELECT r FROM Resume r WHERE r.userId = :userId AND r.isActive = true")
    List<Resume> findByUserIdActive(@Param("userId") String userId);

    @Query("SELECT CASE WHEN COUNT(r) > 0 THEN true ELSE false END FROM Resume r WHERE r.userId = :userId AND r.title = :title AND r.isActive = true")
    boolean existsByUserIdAndTitleActive(@Param("userId") String userId, @Param("title") String title);
    
    @Query("SELECT CASE WHEN COUNT(r) > 0 THEN true ELSE false END FROM Resume r WHERE r.userId = :userId AND r.title = :title AND r.isActive = true AND r.id != :excludeId")
    boolean existsByUserIdAndTitleActiveExcluding(@Param("userId") String userId, @Param("title") String title, @Param("excludeId") String excludeId);
}