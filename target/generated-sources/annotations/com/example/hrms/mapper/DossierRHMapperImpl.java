package com.example.hrms.mapper;

import com.example.hrms.dto.DossierRHDTO;
import com.example.hrms.model.DossierRH;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-02-11T13:52:54+0100",
    comments = "version: 1.5.3.Final, compiler: Eclipse JDT (IDE) 3.45.0.v20260128-0750, environment: Java 21.0.9 (Eclipse Adoptium)"
)
@Component
public class DossierRHMapperImpl implements DossierRHMapper {

    @Override
    public DossierRHDTO toDTO(DossierRH dossierRH) {
        if ( dossierRH == null ) {
            return null;
        }

        DossierRHDTO dossierRHDTO = new DossierRHDTO();

        dossierRHDTO.setIdDossier( dossierRH.getIdDossier() );

        return dossierRHDTO;
    }

    @Override
    public DossierRH toEntity(DossierRHDTO dto) {
        if ( dto == null ) {
            return null;
        }

        DossierRH dossierRH = new DossierRH();

        dossierRH.setIdDossier( dto.getIdDossier() );

        return dossierRH;
    }
}
