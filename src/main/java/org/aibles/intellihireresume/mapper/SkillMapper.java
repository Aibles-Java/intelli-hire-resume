package org.aibles.intellihireresume.mapper;

import org.aibles.intellihireresume.dto.SkillResponse;
import org.aibles.intellihireresume.entity.Skill;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class SkillMapper {

    public SkillResponse toResponse(Skill entity) {
        if (entity == null) {
            return null;
        }
        return SkillResponse.builder()
                .id(entity.getId())
                .name(entity.getName())
                .category(entity.getCategory())
                .type(entity.getType() != null ? entity.getType().name() : null)
                .description(entity.getDescription())
                .build();
    }

    public List<SkillResponse> toResponseList(List<Skill> entities) {
        if (entities == null) {
            return List.of();
        }
        return entities.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }
}
