package com.example.hrms.mapper;

import com.example.hrms.dto.CandidateDTO;
import com.example.hrms.model.Candidate;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface CandidateMapper {

    CandidateDTO toDTO(Candidate candidate);

    Candidate toEntity(CandidateDTO candidateDTO);
}
