package com.example.hrms.mapper;

import com.example.hrms.dto.PlanningDTO;
import com.example.hrms.model.Planning;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface PlanningMapper {

    PlanningDTO toDTO(Planning planning);
    Planning toEntity(PlanningDTO dto);
}
