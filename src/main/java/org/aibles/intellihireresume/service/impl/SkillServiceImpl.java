package org.aibles.intellihireresume.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aibles.intellihireresume.dto.SkillResponse;
import org.aibles.intellihireresume.entity.Skill;
import org.aibles.intellihireresume.exception.ErrorCode;
import org.aibles.intellihireresume.exception.NotFoundException;
import org.aibles.intellihireresume.mapper.SkillMapper;
import org.aibles.intellihireresume.repository.SkillRepository;
import org.aibles.intellihireresume.service.SkillService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SkillServiceImpl implements SkillService {

    private final SkillRepository skillRepository;
    private final SkillMapper skillMapper;

    @Override
    public List<SkillResponse> list(String category) {
        log.info("Listing skills with category: {}", category);
        List<Skill> skills;
        if (category != null && !category.isBlank()) {
            skills = skillRepository.findByCategoryAndIsActiveTrue(category);
        } else {
            skills = skillRepository.findByIsActiveTrue();
        }
        return skillMapper.toResponseList(skills);
    }

    @Override
    public SkillResponse getById(String id) {
        log.info("Getting skill by ID: {}", id);
        Skill skill = skillRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(ErrorCode.SKILL_001));
        return skillMapper.toResponse(skill);
    }

    @Override
    public List<SkillResponse> search(String query) {
        log.info("Searching skills with query: {}", query);
        if (query == null || query.isBlank()) {
            return skillMapper.toResponseList(skillRepository.findByIsActiveTrue());
        }
        return skillMapper.toResponseList(skillRepository.findByNameContainingIgnoreCaseAndIsActiveTrue(query));
    }
}
