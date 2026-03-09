package org.aibles.intellihireresume.service;

import org.aibles.intellihireresume.dto.SkillResponse;

import java.util.List;

public interface SkillService {

    List<SkillResponse> list(String category);

    SkillResponse getById(String id);

    List<SkillResponse> search(String query);
}
