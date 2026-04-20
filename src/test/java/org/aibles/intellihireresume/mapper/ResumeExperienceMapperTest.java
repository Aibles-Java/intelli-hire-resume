package org.aibles.intellihireresume.mapper;

import org.aibles.intellihireresume.dto.ResumeExperienceRequest;
import org.aibles.intellihireresume.dto.ResumeExperienceResponse;
import org.aibles.intellihireresume.entity.ResumeExperience;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ResumeExperienceMapperTest {

    private ResumeExperienceMapper experienceMapper;

    @BeforeEach
    void setUp() {
        experienceMapper = new ResumeExperienceMapper();
    }

    // toEntity tests

    @Test
    void toEntity_ShouldReturnNull_WhenRequestIsNull() {
        ResumeExperience result = experienceMapper.toEntity("resume-1", null);
        assertThat(result).isNull();
    }

    @Test
    void toEntity_ShouldMapAllFields() {
        ResumeExperienceRequest request = ResumeExperienceRequest.builder()
                .company("Acme Corp")
                .title("Engineer")
                .startDate(LocalDate.of(2022, 1, 1))
                .endDate(LocalDate.of(2023, 12, 31))
                .description("Built systems")
                .isCurrent(false)
                .build();

        ResumeExperience result = experienceMapper.toEntity("resume-1", request);

        assertThat(result).isNotNull();
        assertThat(result.getResumeId()).isEqualTo("resume-1");
        assertThat(result.getCompany()).isEqualTo("Acme Corp");
        assertThat(result.getTitle()).isEqualTo("Engineer");
        assertThat(result.getStartDate()).isEqualTo(LocalDate.of(2022, 1, 1));
        assertThat(result.getEndDate()).isEqualTo(LocalDate.of(2023, 12, 31));
        assertThat(result.getDescription()).isEqualTo("Built systems");
        assertThat(result.getIsCurrent()).isFalse();
    }

    @Test
    void toEntity_ShouldDefaultIsCurrentToFalse_WhenIsCurrentIsNull() {
        ResumeExperienceRequest request = ResumeExperienceRequest.builder()
                .company("Acme Corp")
                .title("Engineer")
                .isCurrent(null)
                .build();

        ResumeExperience result = experienceMapper.toEntity("resume-1", request);

        assertThat(result).isNotNull();
        assertThat(result.getIsCurrent()).isFalse();
    }

    @Test
    void toEntity_ShouldMapIsCurrentTrue_WhenRequestHasIsCurrentTrue() {
        ResumeExperienceRequest request = ResumeExperienceRequest.builder()
                .company("Current Corp")
                .title("Senior Engineer")
                .startDate(LocalDate.of(2023, 1, 1))
                .isCurrent(true)
                .build();

        ResumeExperience result = experienceMapper.toEntity("resume-1", request);

        assertThat(result.getIsCurrent()).isTrue();
        assertThat(result.getEndDate()).isNull();
    }

    // updateEntity tests

    @Test
    void updateEntity_ShouldUpdateAllFields() {
        ResumeExperience experience = new ResumeExperience();
        experience.setCompany("Old Corp");
        experience.setTitle("Old Title");
        experience.setIsCurrent(false);

        ResumeExperienceRequest request = ResumeExperienceRequest.builder()
                .company("New Corp")
                .title("New Title")
                .startDate(LocalDate.of(2023, 6, 1))
                .endDate(LocalDate.of(2024, 6, 1))
                .description("New description")
                .isCurrent(false)
                .build();

        experienceMapper.updateEntity(experience, request);

        assertThat(experience.getCompany()).isEqualTo("New Corp");
        assertThat(experience.getTitle()).isEqualTo("New Title");
        assertThat(experience.getStartDate()).isEqualTo(LocalDate.of(2023, 6, 1));
        assertThat(experience.getEndDate()).isEqualTo(LocalDate.of(2024, 6, 1));
        assertThat(experience.getDescription()).isEqualTo("New description");
        assertThat(experience.getIsCurrent()).isFalse();
    }

    // toResponse tests

    @Test
    void toResponse_ShouldReturnNull_WhenEntityIsNull() {
        ResumeExperienceResponse result = experienceMapper.toResponse(null);
        assertThat(result).isNull();
    }

    @Test
    void toResponse_ShouldMapAllFields() {
        LocalDateTime now = LocalDateTime.of(2024, 1, 22, 10, 0, 0);
        ResumeExperience experience = new ResumeExperience();
        experience.setId("exp-1");
        experience.setResumeId("resume-1");
        experience.setCompany("Acme Corp");
        experience.setTitle("Engineer");
        experience.setStartDate(LocalDate.of(2022, 1, 1));
        experience.setEndDate(LocalDate.of(2023, 12, 31));
        experience.setDescription("Built systems");
        experience.setIsCurrent(false);
        experience.setCreatedAt(now);
        experience.setUpdatedAt(now);

        ResumeExperienceResponse result = experienceMapper.toResponse(experience);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo("exp-1");
        assertThat(result.getResumeId()).isEqualTo("resume-1");
        assertThat(result.getCompany()).isEqualTo("Acme Corp");
        assertThat(result.getTitle()).isEqualTo("Engineer");
        assertThat(result.getStartDate()).isEqualTo(LocalDate.of(2022, 1, 1));
        assertThat(result.getEndDate()).isEqualTo(LocalDate.of(2023, 12, 31));
        assertThat(result.getDescription()).isEqualTo("Built systems");
        assertThat(result.getIsCurrent()).isFalse();
        assertThat(result.getCreatedAt()).isEqualTo(now);
        assertThat(result.getUpdatedAt()).isEqualTo(now);
    }

    // toResponseList tests

    @Test
    void toResponseList_ShouldReturnEmptyList_WhenListIsNull() {
        List<ResumeExperienceResponse> result = experienceMapper.toResponseList(null);
        assertThat(result).isEmpty();
    }

    @Test
    void toResponseList_ShouldReturnEmptyList_WhenListIsEmpty() {
        List<ResumeExperienceResponse> result = experienceMapper.toResponseList(Collections.emptyList());
        assertThat(result).isEmpty();
    }

    @Test
    void toResponseList_ShouldMapAllItemsInList() {
        ResumeExperience exp1 = new ResumeExperience();
        exp1.setId("exp-1");
        exp1.setResumeId("resume-1");
        exp1.setCompany("Corp A");
        exp1.setTitle("Dev");
        exp1.setIsCurrent(false);

        ResumeExperience exp2 = new ResumeExperience();
        exp2.setId("exp-2");
        exp2.setResumeId("resume-1");
        exp2.setCompany("Corp B");
        exp2.setTitle("Senior Dev");
        exp2.setIsCurrent(true);

        List<ResumeExperienceResponse> result = experienceMapper.toResponseList(List.of(exp1, exp2));

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getId()).isEqualTo("exp-1");
        assertThat(result.get(0).getCompany()).isEqualTo("Corp A");
        assertThat(result.get(1).getId()).isEqualTo("exp-2");
        assertThat(result.get(1).getCompany()).isEqualTo("Corp B");
        assertThat(result.get(1).getIsCurrent()).isTrue();
    }
}
