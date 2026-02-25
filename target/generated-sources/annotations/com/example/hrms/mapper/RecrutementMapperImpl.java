package com.example.hrms.mapper;

import com.example.hrms.dto.RecrutementDTO;
import com.example.hrms.model.Recrutement;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-02-11T13:52:54+0100",
    comments = "version: 1.5.3.Final, compiler: Eclipse JDT (IDE) 3.45.0.v20260128-0750, environment: Java 21.0.9 (Eclipse Adoptium)"
)
@Component
public class RecrutementMapperImpl implements RecrutementMapper {

    @Override
    public RecrutementDTO toDTO(Recrutement recrutement) {
        if ( recrutement == null ) {
            return null;
        }

        RecrutementDTO recrutementDTO = new RecrutementDTO();

        recrutementDTO.setIdRecrutement( recrutement.getIdRecrutement() );
        recrutementDTO.setPosteCible( recrutement.getPosteCible() );
        recrutementDTO.setStatut( recrutement.getStatut() );

        return recrutementDTO;
    }

    @Override
    public Recrutement toEntity(RecrutementDTO recrutementDTO) {
        if ( recrutementDTO == null ) {
            return null;
        }

        Recrutement recrutement = new Recrutement();

        recrutement.setIdRecrutement( recrutementDTO.getIdRecrutement() );
        recrutement.setPosteCible( recrutementDTO.getPosteCible() );
        recrutement.setStatut( recrutementDTO.getStatut() );

        return recrutement;
    }
}
