package com.example.hrms.mapper;

import com.example.hrms.dto.FormationDTO;
import com.example.hrms.model.Formation;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-02-11T13:52:54+0100",
    comments = "version: 1.5.3.Final, compiler: Eclipse JDT (IDE) 3.45.0.v20260128-0750, environment: Java 21.0.9 (Eclipse Adoptium)"
)
@Component
public class FormationMapperImpl implements FormationMapper {

    @Override
    public FormationDTO toDTO(Formation formation) {
        if ( formation == null ) {
            return null;
        }

        FormationDTO formationDTO = new FormationDTO();

        formationDTO.setCertification( formation.getCertification() );
        formationDTO.setIdFormation( formation.getIdFormation() );
        formationDTO.setTitre( formation.getTitre() );

        return formationDTO;
    }

    @Override
    public Formation toEntity(FormationDTO dto) {
        if ( dto == null ) {
            return null;
        }

        Formation formation = new Formation();

        formation.setCertification( dto.getCertification() );
        formation.setIdFormation( dto.getIdFormation() );
        formation.setTitre( dto.getTitre() );

        return formation;
    }
}
