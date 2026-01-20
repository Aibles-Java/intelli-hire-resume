package org.aibles.intellihireresume.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aibles.intellihireresume.repository.ResumeParseJobRepository;
import org.aibles.intellihireresume.service.ResumeParseJobService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ResumeParseJobServiceImpl implements ResumeParseJobService {
    
    private final ResumeParseJobRepository resumeParseJobRepository;
}