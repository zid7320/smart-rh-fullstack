package com.example.hrms.mapper;

import com.example.hrms.dto.CandidateDTO;
import com.example.hrms.model.Candidate;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-02-11T13:52:54+0100",
    comments = "version: 1.5.3.Final, compiler: Eclipse JDT (IDE) 3.45.0.v20260128-0750, environment: Java 21.0.9 (Eclipse Adoptium)"
)
@Component
public class CandidateMapperImpl implements CandidateMapper {

    @Override
    public CandidateDTO toDTO(Candidate candidate) {
        if ( candidate == null ) {
            return null;
        }

        CandidateDTO candidateDTO = new CandidateDTO();

        candidateDTO.setEmail( candidate.getEmail() );
        candidateDTO.setIdCandidate( candidate.getIdCandidate() );
        candidateDTO.setNom( candidate.getNom() );
        candidateDTO.setPrenom( candidate.getPrenom() );

        return candidateDTO;
    }

    @Override
    public Candidate toEntity(CandidateDTO candidateDTO) {
        if ( candidateDTO == null ) {
            return null;
        }

        Candidate candidate = new Candidate();

        candidate.setEmail( candidateDTO.getEmail() );
        candidate.setIdCandidate( candidateDTO.getIdCandidate() );
        candidate.setNom( candidateDTO.getNom() );
        candidate.setPrenom( candidateDTO.getPrenom() );

        return candidate;
    }
}
