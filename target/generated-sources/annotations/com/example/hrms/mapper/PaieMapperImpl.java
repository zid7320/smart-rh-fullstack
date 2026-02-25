package com.example.hrms.mapper;

import com.example.hrms.dto.PaieDTO;
import com.example.hrms.model.Paie;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-02-11T13:52:54+0100",
    comments = "version: 1.5.3.Final, compiler: Eclipse JDT (IDE) 3.45.0.v20260128-0750, environment: Java 21.0.9 (Eclipse Adoptium)"
)
@Component
public class PaieMapperImpl implements PaieMapper {

    @Override
    public PaieDTO toDTO(Paie paie) {
        if ( paie == null ) {
            return null;
        }

        PaieDTO paieDTO = new PaieDTO();

        paieDTO.setIdPaie( paie.getIdPaie() );
        if ( paie.getMontant() != null ) {
            paieDTO.setMontant( paie.getMontant() );
        }

        return paieDTO;
    }

    @Override
    public Paie toEntity(PaieDTO dto) {
        if ( dto == null ) {
            return null;
        }

        Paie paie = new Paie();

        paie.setIdPaie( dto.getIdPaie() );
        paie.setMontant( dto.getMontant() );

        return paie;
    }
}
