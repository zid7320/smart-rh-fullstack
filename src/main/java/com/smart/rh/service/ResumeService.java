package com.smart.rh.service;

import com.smart.rh.dto.recruitment.ResumeDto;
import com.smart.rh.entity.Resume;
import com.smart.rh.mapper.ResumeMapper;
import com.smart.rh.repository.ResumeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class ResumeService {

    private final ResumeRepository resumeRepository;
    private final ResumeMapper resumeMapper;

    public ResumeDto create(ResumeDto dto) {
        Resume resume = resumeMapper.toEntity(dto);
        Resume saved = resumeRepository.save(resume);
        return resumeMapper.toDto(saved);
    }

    public ResumeDto update(Long id, ResumeDto dto) {
        Resume resume = resumeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Resume not found"));
        resumeMapper.updateEntity(dto, resume);
        Resume updated = resumeRepository.save(resume);
        return resumeMapper.toDto(updated);
    }

    public void delete(Long id) {
        resumeRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public Optional<ResumeDto> findById(Long id) {
        return resumeRepository.findById(id).map(resumeMapper::toDto);
    }

    @Transactional(readOnly = true)
    public List<ResumeDto> findByEmail(String email) {
        return resumeRepository.findByEmail(email)
                .stream()
                .map(resumeMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ResumeDto> findBySenderEmail(String senderEmail) {
        return resumeRepository.findBySenderEmail(senderEmail)
                .stream()
                .map(resumeMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ResumeDto> findByReceivedAtBetween(LocalDateTime startDate, LocalDateTime endDate) {
        return resumeRepository.findByReceivedAtBetween(startDate, endDate)
                .stream()
                .map(resumeMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ResumeDto> findAll() {
        return resumeRepository.findAllOrderByReceivedAtDesc()
                .stream()
                .map(resumeMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Long count() {
        return resumeRepository.count();
    }
}
