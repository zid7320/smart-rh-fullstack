package com.example.hrms.mapper;

import com.example.hrms.dto.RecrutementDTO;
import com.example.hrms.model.Recrutement;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface RecrutementMapper {

    RecrutementDTO toDTO(Recrutement recrutement);

    Recrutement toEntity(RecrutementDTO recrutementDTO);
}
