package com.example.hrms.mapper;

import com.example.hrms.dto.ContratDTO;
import com.example.hrms.model.Contrat;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-02-11T13:52:54+0100",
    comments = "version: 1.5.3.Final, compiler: Eclipse JDT (IDE) 3.45.0.v20260128-0750, environment: Java 21.0.9 (Eclipse Adoptium)"
)
@Component
public class ContratMapperImpl implements ContratMapper {

    @Override
    public ContratDTO toDTO(Contrat contrat) {
        if ( contrat == null ) {
            return null;
        }

        ContratDTO contratDTO = new ContratDTO();

        contratDTO.setDateDebut( contrat.getDateDebut() );
        contratDTO.setDateFin( contrat.getDateFin() );
        contratDTO.setIdContrat( contrat.getIdContrat() );
        contratDTO.setSalaire( contrat.getSalaire() );
        contratDTO.setType( contrat.getType() );

        return contratDTO;
    }

    @Override
    public Contrat toEntity(ContratDTO contratDTO) {
        if ( contratDTO == null ) {
            return null;
        }

        Contrat contrat = new Contrat();

        contrat.setDateDebut( contratDTO.getDateDebut() );
        contrat.setDateFin( contratDTO.getDateFin() );
        contrat.setIdContrat( contratDTO.getIdContrat() );
        contrat.setSalaire( contratDTO.getSalaire() );
        contrat.setType( contratDTO.getType() );

        return contrat;
    }
}
