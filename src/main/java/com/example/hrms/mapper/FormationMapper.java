package com.example.hrms.mapper;

import com.example.hrms.dto.FormationDTO;
import com.example.hrms.model.Formation;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface FormationMapper {

    FormationDTO toDTO(Formation formation);
    Formation toEntity(FormationDTO dto);
}
