package com.example.hrms.mapper;

import com.example.hrms.dto.CompetenceDTO;
import com.example.hrms.model.Competence;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-02-11T13:52:54+0100",
    comments = "version: 1.5.3.Final, compiler: Eclipse JDT (IDE) 3.45.0.v20260128-0750, environment: Java 21.0.9 (Eclipse Adoptium)"
)
@Component
public class CompetenceMapperImpl implements CompetenceMapper {

    @Override
    public CompetenceDTO toDTO(Competence competence) {
        if ( competence == null ) {
            return null;
        }

        CompetenceDTO competenceDTO = new CompetenceDTO();

        competenceDTO.setIdCompetence( competence.getIdCompetence() );
        competenceDTO.setNiveau( competence.getNiveau() );
        competenceDTO.setNom( competence.getNom() );

        return competenceDTO;
    }

    @Override
    public Competence toEntity(CompetenceDTO competenceDTO) {
        if ( competenceDTO == null ) {
            return null;
        }

        Competence competence = new Competence();

        competence.setIdCompetence( competenceDTO.getIdCompetence() );
        competence.setNiveau( competenceDTO.getNiveau() );
        competence.setNom( competenceDTO.getNom() );

        return competence;
    }
}
