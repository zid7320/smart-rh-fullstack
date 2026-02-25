package com.example.hrms.mapper;

import com.example.hrms.dto.PosteDTO;
import com.example.hrms.model.Poste;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-02-11T13:52:54+0100",
    comments = "version: 1.5.3.Final, compiler: Eclipse JDT (IDE) 3.45.0.v20260128-0750, environment: Java 21.0.9 (Eclipse Adoptium)"
)
@Component
public class PosteMapperImpl implements PosteMapper {

    @Override
    public PosteDTO toDTO(Poste poste) {
        if ( poste == null ) {
            return null;
        }

        PosteDTO posteDTO = new PosteDTO();

        posteDTO.setCompetencesRequises( poste.getCompetencesRequises() );
        posteDTO.setIdPoste( poste.getIdPoste() );
        posteDTO.setTitre( poste.getTitre() );

        return posteDTO;
    }

    @Override
    public Poste toEntity(PosteDTO posteDTO) {
        if ( posteDTO == null ) {
            return null;
        }

        Poste poste = new Poste();

        poste.setCompetencesRequises( posteDTO.getCompetencesRequises() );
        poste.setIdPoste( posteDTO.getIdPoste() );
        poste.setTitre( posteDTO.getTitre() );

        return poste;
    }
}
