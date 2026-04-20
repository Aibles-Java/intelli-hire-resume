package org.aibles.intellihireresume.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aibles.intellihireresume.dto.BaseResponse;
import org.aibles.intellihireresume.dto.SkillResponse;
import org.aibles.intellihireresume.service.SkillService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/skills")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class SkillController {

    private final SkillService skillService;

    @GetMapping
    public ResponseEntity<BaseResponse<List<SkillResponse>>> list(
            @RequestParam(required = false) String category) {
        log.info("Listing skills with category: {}", category);
        List<SkillResponse> response = skillService.list(category);
        return ResponseEntity.ok(BaseResponse.success(response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<BaseResponse<SkillResponse>> getById(@PathVariable String id) {
        log.info("Getting skill by ID: {}", id);
        SkillResponse response = skillService.getById(id);
        return ResponseEntity.ok(BaseResponse.success(response));
    }

    @GetMapping("/search")
    public ResponseEntity<BaseResponse<List<SkillResponse>>> search(
            @RequestParam(name = "q") String query) {
        log.info("Searching skills with query: {}", query);
        List<SkillResponse> response = skillService.search(query);
        return ResponseEntity.ok(BaseResponse.success(response));
    }
}
