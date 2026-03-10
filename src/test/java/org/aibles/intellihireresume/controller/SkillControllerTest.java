package org.aibles.intellihireresume.controller;

import org.aibles.intellihireresume.dto.SkillResponse;
import org.aibles.intellihireresume.exception.ErrorCode;
import org.aibles.intellihireresume.exception.NotFoundException;
import org.aibles.intellihireresume.service.SkillService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = SkillController.class)
class SkillControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SkillService skillService;

    private SkillResponse skillResponse;

    @BeforeEach
    void setUp() {
        skillResponse = SkillResponse.builder()
                .id("skill-001")
                .name("Java")
                .category("Programming Languages")
                .type("TECHNICAL")
                .description("Object-oriented programming language")
                .build();
    }

    @Test
    void list_ShouldReturn200_WhenNoCategory() throws Exception {
        when(skillService.list(null)).thenReturn(List.of(skillResponse));

        mockMvc.perform(get("/api/v1/skills")
                        .header("X-User-Id", "test-user"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].id").value("skill-001"))
                .andExpect(jsonPath("$.data[0].name").value("Java"));

        verify(skillService).list(null);
    }

    @Test
    void list_ShouldReturn200_WhenCategoryProvided() throws Exception {
        when(skillService.list("Programming Languages")).thenReturn(List.of(skillResponse));

        mockMvc.perform(get("/api/v1/skills").param("category", "Programming Languages")
                        .header("X-User-Id", "test-user"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].category").value("Programming Languages"));

        verify(skillService).list("Programming Languages");
    }

    @Test
    void getById_ShouldReturn200_WhenSkillExists() throws Exception {
        when(skillService.getById("skill-001")).thenReturn(skillResponse);

        mockMvc.perform(get("/api/v1/skills/{id}", "skill-001")
                        .header("X-User-Id", "test-user"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value("skill-001"))
                .andExpect(jsonPath("$.data.name").value("Java"));

        verify(skillService).getById("skill-001");
    }

    @Test
    void getById_ShouldReturn404_WhenSkillNotFound() throws Exception {
        when(skillService.getById("nonexistent")).thenThrow(new NotFoundException(ErrorCode.SKILL_001));

        mockMvc.perform(get("/api/v1/skills/{id}", "nonexistent")
                        .header("X-User-Id", "test-user"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error_code").value(ErrorCode.SKILL_001.getCode()));
    }

    @Test
    void search_ShouldReturn200_WhenQueryProvided() throws Exception {
        when(skillService.search("java")).thenReturn(List.of(skillResponse));

        mockMvc.perform(get("/api/v1/skills/search").param("q", "java")
                        .header("X-User-Id", "test-user"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].name").value("Java"));

        verify(skillService).search("java");
    }
}
