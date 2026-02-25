package com.example.hrms.mapper;

import com.example.hrms.dto.ResponsableRHDTO;
import com.example.hrms.model.ResponsableRH;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-02-11T13:52:54+0100",
    comments = "version: 1.5.3.Final, compiler: Eclipse JDT (IDE) 3.45.0.v20260128-0750, environment: Java 21.0.9 (Eclipse Adoptium)"
)
@Component
public class ResponsableRHMapperImpl implements ResponsableRHMapper {

    @Override
    public ResponsableRHDTO toDTO(ResponsableRH responsableRH) {
        if ( responsableRH == null ) {
            return null;
        }

        ResponsableRHDTO responsableRHDTO = new ResponsableRHDTO();

        responsableRHDTO.setIdResponsable( responsableRH.getIdResponsable() );
        responsableRHDTO.setNom( responsableRH.getNom() );

        return responsableRHDTO;
    }

    @Override
    public ResponsableRH toEntity(ResponsableRHDTO responsableRHDTO) {
        if ( responsableRHDTO == null ) {
            return null;
        }

        ResponsableRH responsableRH = new ResponsableRH();

        responsableRH.setIdResponsable( responsableRHDTO.getIdResponsable() );
        responsableRH.setNom( responsableRHDTO.getNom() );

        return responsableRH;
    }
}
