package com.smart.rh.service;

import com.smart.rh.dto.recruitment.CandidateAnalysisDto;
import com.smart.rh.entity.CandidateAnalysis;
import com.smart.rh.mapper.CandidateAnalysisMapper;
import com.smart.rh.repository.CandidateAnalysisRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class CandidateAnalysisService {

    private final CandidateAnalysisRepository candidateAnalysisRepository;
    private final CandidateAnalysisMapper candidateAnalysisMapper;

    public CandidateAnalysisDto create(CandidateAnalysisDto dto) {
        CandidateAnalysis analysis = candidateAnalysisMapper.toEntity(dto);
        CandidateAnalysis saved = candidateAnalysisRepository.save(analysis);
        return candidateAnalysisMapper.toDto(saved);
    }

    public CandidateAnalysisDto update(Long id, CandidateAnalysisDto dto) {
        CandidateAnalysis analysis = candidateAnalysisRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Candidate analysis not found"));
        candidateAnalysisMapper.updateEntity(dto, analysis);
        CandidateAnalysis updated = candidateAnalysisRepository.save(analysis);
        return candidateAnalysisMapper.toDto(updated);
    }

    public void delete(Long id) {
        candidateAnalysisRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public Optional<CandidateAnalysisDto> findById(Long id) {
        return candidateAnalysisRepository.findById(id).map(candidateAnalysisMapper::toDto);
    }

    @Transactional(readOnly = true)
    public List<CandidateAnalysisDto> findByJobId(Integer jobId) {
        return candidateAnalysisRepository.findByJobIdOrderByScore(jobId)
                .stream()
                .map(candidateAnalysisMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CandidateAnalysisDto> findByEmail(String email) {
        return candidateAnalysisRepository.findByEmail(email)
                .stream()
                .map(candidateAnalysisMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CandidateAnalysisDto> findByJobIdAndRecommendation(Integer jobId, String recommendation) {
        return candidateAnalysisRepository.findByJobIdAndRecommendation(jobId, recommendation)
                .stream()
                .map(candidateAnalysisMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CandidateAnalysisDto> findAll() {
        return candidateAnalysisRepository.findAll()
                .stream()
                .map(candidateAnalysisMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Long count() {
        return candidateAnalysisRepository.count();
    }
}
