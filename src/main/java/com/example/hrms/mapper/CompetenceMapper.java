package com.example.hrms.mapper;

import com.example.hrms.dto.CompetenceDTO;
import com.example.hrms.model.Competence;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface CompetenceMapper {

    CompetenceDTO toDTO(Competence competence);

    Competence toEntity(CompetenceDTO competenceDTO);
}
