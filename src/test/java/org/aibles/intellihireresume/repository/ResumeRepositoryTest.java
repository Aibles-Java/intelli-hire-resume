package org.aibles.intellihireresume.repository;

import org.aibles.intellihireresume.entity.Resume;
import org.aibles.intellihireresume.entity.enums.ResumeStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class ResumeRepositoryTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ResumeRepository resumeRepository;

    private Resume activeResume;
    private Resume inactiveResume;
    private String testUserId;
    private String testResumeId;

    @BeforeEach
    void setUp() {
        testUserId = "test-user-123";
        testResumeId = "test-resume-456";
        
        // Create and persist active resume
        activeResume = Resume.builder()
                .id(testResumeId)
                .userId(testUserId)
                .title("Software Engineer Resume")
                .status(ResumeStatus.COMPLETED)
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .createdBy("test-user")
                .updatedBy("test-user")
                .build();

        // Create and persist inactive resume
        inactiveResume = Resume.builder()
                .id("inactive-resume-789")
                .userId(testUserId)
                .title("Old Resume")
                .status(ResumeStatus.FAILED)
                .isActive(false)
                .createdAt(LocalDateTime.now().minusDays(1))
                .updatedAt(LocalDateTime.now().minusDays(1))
                .createdBy("test-user")
                .updatedBy("test-user")
                .build();

        entityManager.persistAndFlush(activeResume);
        entityManager.persistAndFlush(inactiveResume);
    }

    @Test
    void findByIdActive_ShouldReturnActiveResume_WhenResumeExists() {
        // When
        Optional<Resume> result = resumeRepository.findByIdActive(testResumeId);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(testResumeId);
        assertThat(result.get().getIsActive()).isTrue();
        assertThat(result.get().getUserId()).isEqualTo(testUserId);
        assertThat(result.get().getTitle()).isEqualTo("Software Engineer Resume");
        assertThat(result.get().getStatus()).isEqualTo(ResumeStatus.COMPLETED);
    }

    @Test
    void findByIdActive_ShouldReturnEmpty_WhenResumeIsInactive() {
        // When
        Optional<Resume> result = resumeRepository.findByIdActive("inactive-resume-789");

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void findByIdActive_ShouldReturnEmpty_WhenResumeNotExists() {
        // When
        Optional<Resume> result = resumeRepository.findByIdActive("non-existent-id");

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void findByUserIdActive_ShouldReturnActiveResumesOnly() {
        // Given
        Resume secondActiveResume = Resume.builder()
                .id("newer-resume-101")
                .userId(testUserId)
                .title("Newer Resume")
                .status(ResumeStatus.UPLOADED)
                .isActive(true)
                .createdAt(LocalDateTime.now().plusMinutes(1))
                .updatedAt(LocalDateTime.now().plusMinutes(1))
                .createdBy("test-user")
                .updatedBy("test-user")
                .build();

        entityManager.persistAndFlush(secondActiveResume);

        // When
        List<Resume> results = resumeRepository.findByUserIdActive(testUserId);

        // Then
        assertThat(results).hasSize(2);
        assertThat(results).allMatch(resume -> resume.getIsActive().equals(true));
        assertThat(results).allMatch(resume -> resume.getUserId().equals(testUserId));
        
        // Should not include inactive resume
        assertThat(results).noneMatch(resume -> resume.getId().equals("inactive-resume-789"));
        
        // Should include both active resumes
        assertThat(results).anyMatch(resume -> resume.getId().equals(testResumeId));
        assertThat(results).anyMatch(resume -> resume.getId().equals("newer-resume-101"));
    }

    @Test
    void findByUserIdActive_ShouldReturnEmptyList_WhenNoActiveResumes() {
        // When
        List<Resume> results = resumeRepository.findByUserIdActive("non-existent-user");

        // Then
        assertThat(results).isEmpty();
    }

    @Test
    void findByUserIdActive_ShouldReturnEmptyList_WhenOnlyInactiveResumes() {
        // Given - create user with only inactive resumes
        String inactiveOnlyUserId = "inactive-only-user";
        Resume inactiveOnly = Resume.builder()
                .id("inactive-only-resume")
                .userId(inactiveOnlyUserId)
                .title("Inactive Resume")
                .status(ResumeStatus.FAILED)
                .isActive(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .createdBy("test-user")
                .updatedBy("test-user")
                .build();

        entityManager.persistAndFlush(inactiveOnly);

        // When
        List<Resume> results = resumeRepository.findByUserIdActive(inactiveOnlyUserId);

        // Then
        assertThat(results).isEmpty();
    }

    @Test
    void existsByUserIdAndTitleActive_ShouldReturnTrue_WhenActiveTitleExists() {
        // When
        boolean exists = resumeRepository.existsByUserIdAndTitleActive(testUserId, "Software Engineer Resume");

        // Then
        assertThat(exists).isTrue();
    }

    @Test
    void existsByUserIdAndTitleActive_ShouldReturnFalse_WhenTitleExistsButInactive() {
        // When
        boolean exists = resumeRepository.existsByUserIdAndTitleActive(testUserId, "Old Resume");

        // Then
        assertThat(exists).isFalse();
    }

    @Test
    void existsByUserIdAndTitleActive_ShouldReturnFalse_WhenTitleNotExists() {
        // When
        boolean exists = resumeRepository.existsByUserIdAndTitleActive(testUserId, "Non-existent Title");

        // Then
        assertThat(exists).isFalse();
    }

    @Test
    void existsByUserIdAndTitleActive_ShouldReturnFalse_WhenUserNotExists() {
        // When
        boolean exists = resumeRepository.existsByUserIdAndTitleActive("non-existent-user", "Software Engineer Resume");

        // Then
        assertThat(exists).isFalse();
    }

    @Test
    void save_ShouldPersistNewResume() {
        // Given
        Resume newResume = Resume.builder()
                .id("new-resume-123")
                .userId("new-user-456")
                .title("New Resume")
                .status(ResumeStatus.UPLOADED)
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .createdBy("test-user")
                .updatedBy("test-user")
                .build();

        // When
        Resume savedResume = resumeRepository.save(newResume);

        // Then
        assertThat(savedResume.getId()).isNotNull();
        assertThat(savedResume.getUserId()).isEqualTo("new-user-456");
        assertThat(savedResume.getTitle()).isEqualTo("New Resume");
        assertThat(savedResume.getStatus()).isEqualTo(ResumeStatus.UPLOADED);
        assertThat(savedResume.getIsActive()).isTrue();

        // Verify it can be found in database
        Optional<Resume> foundResume = resumeRepository.findById("new-resume-123");
        assertThat(foundResume).isPresent();
        assertThat(foundResume.get().getTitle()).isEqualTo("New Resume");
    }

    @Test
    void save_ShouldUpdateExistingResume() {
        // Given
        activeResume.setTitle("Updated Resume Title");
        activeResume.setStatus(ResumeStatus.COMPLETED);
        activeResume.setUpdatedAt(LocalDateTime.now());

        // When
        Resume savedResume = resumeRepository.save(activeResume);

        // Then
        assertThat(savedResume.getId()).isEqualTo(testResumeId);
        assertThat(savedResume.getTitle()).isEqualTo("Updated Resume Title");
        assertThat(savedResume.getStatus()).isEqualTo(ResumeStatus.COMPLETED);

        // Verify changes are persisted
        Optional<Resume> foundResume = resumeRepository.findById(testResumeId);
        assertThat(foundResume).isPresent();
        assertThat(foundResume.get().getTitle()).isEqualTo("Updated Resume Title");
    }

    @Test
    void save_ShouldHandleSoftDelete_WhenIsActiveSetToFalse() {
        // Given
        activeResume.setIsActive(false);
        activeResume.setUpdatedAt(LocalDateTime.now());

        // When
        Resume savedResume = resumeRepository.save(activeResume);

        // Then
        assertThat(savedResume.getIsActive()).isFalse();
        assertThat(savedResume.getId()).isEqualTo(testResumeId);

        // Verify soft delete - should not be found by findByIdActive
        Optional<Resume> foundActiveResume = resumeRepository.findByIdActive(testResumeId);
        assertThat(foundActiveResume).isEmpty();

        // But should still exist in database
        Optional<Resume> foundResume = resumeRepository.findById(testResumeId);
        assertThat(foundResume).isPresent();
        assertThat(foundResume.get().getIsActive()).isFalse();
    }

    @Test
    void findAll_ShouldReturnAllResumes_IncludingInactive() {
        // When
        List<Resume> allResumes = resumeRepository.findAll();

        // Then
        assertThat(allResumes).hasSize(2);
        assertThat(allResumes).anyMatch(resume -> resume.getIsActive().equals(true));
        assertThat(allResumes).anyMatch(resume -> resume.getIsActive().equals(false));
    }

    @Test
    void deleteById_ShouldHardDeleteResume() {
        // Given
        String resumeToDeleteId = activeResume.getId();

        // When
        resumeRepository.deleteById(resumeToDeleteId);
        resumeRepository.flush();

        // Then
        Optional<Resume> foundResume = resumeRepository.findById(resumeToDeleteId);
        assertThat(foundResume).isEmpty();

        // Should also not be found by active query
        Optional<Resume> foundActiveResume = resumeRepository.findByIdActive(resumeToDeleteId);
        assertThat(foundActiveResume).isEmpty();
    }

    @Test
    void findByUserIdActive_ShouldHandleNullTitle() {
        // Given
        Resume resumeWithNullTitle = Resume.builder()
                .id("null-title-resume")
                .userId(testUserId)
                .title(null)
                .status(ResumeStatus.UPLOADED)
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .createdBy("test-user")
                .updatedBy("test-user")
                .build();

        entityManager.persistAndFlush(resumeWithNullTitle);

        // When
        List<Resume> results = resumeRepository.findByUserIdActive(testUserId);

        // Then
        assertThat(results).hasSize(2); // active resume + null title resume
        assertThat(results).anyMatch(resume -> resume.getTitle() == null);
        assertThat(results).anyMatch(resume -> resume.getId().equals("null-title-resume"));
    }

    @Test
    void existsByUserIdAndTitleActive_ShouldHandleNullTitle() {
        // Given - create resume with null title
        Resume resumeWithNullTitle = Resume.builder()
                .id("null-title-resume")
                .userId(testUserId)
                .title(null)
                .status(ResumeStatus.UPLOADED)
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .createdBy("test-user")
                .updatedBy("test-user")
                .build();

        entityManager.persistAndFlush(resumeWithNullTitle);

        boolean existsWithNull = resumeRepository.existsByUserIdAndTitleActive(testUserId, null);
        
        assertThat(existsWithNull).isFalse();
    }
}