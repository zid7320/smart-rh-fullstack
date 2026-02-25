package com.example.hrms.mapper;

import com.example.hrms.dto.CongeDTO;
import com.example.hrms.model.Conge;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-02-11T13:52:54+0100",
    comments = "version: 1.5.3.Final, compiler: Eclipse JDT (IDE) 3.45.0.v20260128-0750, environment: Java 21.0.9 (Eclipse Adoptium)"
)
@Component
public class CongeMapperImpl implements CongeMapper {

    @Override
    public CongeDTO toDTO(Conge conge) {
        if ( conge == null ) {
            return null;
        }

        CongeDTO congeDTO = new CongeDTO();

        congeDTO.setDateDebut( conge.getDateDebut() );
        congeDTO.setDateFin( conge.getDateFin() );
        congeDTO.setIdConge( conge.getIdConge() );
        congeDTO.setType( conge.getType() );

        return congeDTO;
    }

    @Override
    public Conge toEntity(CongeDTO congeDTO) {
        if ( congeDTO == null ) {
            return null;
        }

        Conge conge = new Conge();

        conge.setDateDebut( congeDTO.getDateDebut() );
        conge.setDateFin( congeDTO.getDateFin() );
        conge.setIdConge( congeDTO.getIdConge() );
        conge.setType( congeDTO.getType() );

        return conge;
    }
}
