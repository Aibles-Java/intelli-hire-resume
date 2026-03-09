package org.aibles.intellihireresume.service.impl;

import org.aibles.intellihireresume.dto.SkillResponse;
import org.aibles.intellihireresume.entity.Skill;
import org.aibles.intellihireresume.entity.enums.SkillType;
import org.aibles.intellihireresume.exception.ErrorCode;
import org.aibles.intellihireresume.exception.NotFoundException;
import org.aibles.intellihireresume.mapper.SkillMapper;
import org.aibles.intellihireresume.repository.SkillRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SkillServiceImplTest {

    @Mock
    private SkillRepository skillRepository;

    private SkillMapper skillMapper = new SkillMapper();

    private SkillServiceImpl skillService;

    private Skill testSkill;

    @BeforeEach
    void setUp() {
        skillService = new SkillServiceImpl(skillRepository, skillMapper);

        testSkill = new Skill();
        testSkill.setId("skill-001");
        testSkill.setName("Java");
        testSkill.setCategory("Programming Languages");
        testSkill.setType(SkillType.TECHNICAL);
        testSkill.setDescription("Object-oriented programming language");
        testSkill.setIsActive(true);
    }

    @Test
    void list_ShouldReturnAll_WhenNoCategoryFilter() {
        when(skillRepository.findByIsActiveTrue()).thenReturn(List.of(testSkill));

        List<SkillResponse> result = skillService.list(null);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Java");
        verify(skillRepository).findByIsActiveTrue();
        verify(skillRepository, never()).findByCategoryAndIsActiveTrue(any());
    }

    @Test
    void list_ShouldReturnFiltered_WhenCategoryProvided() {
        when(skillRepository.findByCategoryAndIsActiveTrue("Programming Languages")).thenReturn(List.of(testSkill));

        List<SkillResponse> result = skillService.list("Programming Languages");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCategory()).isEqualTo("Programming Languages");
        verify(skillRepository).findByCategoryAndIsActiveTrue("Programming Languages");
        verify(skillRepository, never()).findByIsActiveTrue();
    }

    @Test
    void list_ShouldReturnAll_WhenCategoryIsBlank() {
        when(skillRepository.findByIsActiveTrue()).thenReturn(List.of(testSkill));

        List<SkillResponse> result = skillService.list("   ");

        assertThat(result).hasSize(1);
        verify(skillRepository).findByIsActiveTrue();
    }

    @Test
    void getById_ShouldReturnSkill_WhenExists() {
        when(skillRepository.findById("skill-001")).thenReturn(Optional.of(testSkill));

        SkillResponse result = skillService.getById("skill-001");

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo("skill-001");
        assertThat(result.getName()).isEqualTo("Java");
        assertThat(result.getType()).isEqualTo("TECHNICAL");
    }

    @Test
    void getById_ShouldThrowNotFoundException_WhenNotFound() {
        when(skillRepository.findById("nonexistent")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> skillService.getById("nonexistent"))
                .isInstanceOf(NotFoundException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.SKILL_001.getCode());
    }

    @Test
    void search_ShouldReturnResults_WhenQueryProvided() {
        when(skillRepository.findByNameContainingIgnoreCaseAndIsActiveTrue("java")).thenReturn(List.of(testSkill));

        List<SkillResponse> result = skillService.search("java");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Java");
        verify(skillRepository).findByNameContainingIgnoreCaseAndIsActiveTrue("java");
    }

    @Test
    void search_ShouldReturnAll_WhenQueryIsBlank() {
        when(skillRepository.findByIsActiveTrue()).thenReturn(List.of(testSkill));

        List<SkillResponse> result = skillService.search("");

        assertThat(result).hasSize(1);
        verify(skillRepository).findByIsActiveTrue();
        verify(skillRepository, never()).findByNameContainingIgnoreCaseAndIsActiveTrue(any());
    }
}
