package com.smart.rh.mapper;

import com.smart.rh.dto.recruitment.CandidateAnalysisDto;
import com.smart.rh.entity.CandidateAnalysis;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CandidateAnalysisMapper {

    @Mapping(target = "createdAt", ignore = true)
    CandidateAnalysisDto toDto(CandidateAnalysis entity);

    @Mapping(target = "createdAt", ignore = true)
    CandidateAnalysis toEntity(CandidateAnalysisDto dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "createdAt", ignore = true)
    void updateEntity(CandidateAnalysisDto dto, @MappingTarget CandidateAnalysis entity);

    List<CandidateAnalysisDto> toDtoList(List<CandidateAnalysis> entities);

    List<CandidateAnalysis> toEntityList(List<CandidateAnalysisDto> dtos);
}
