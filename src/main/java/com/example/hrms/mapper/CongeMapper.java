package com.example.hrms.mapper;

import com.example.hrms.dto.CongeDTO;
import com.example.hrms.model.Conge;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface CongeMapper {

    CongeDTO toDTO(Conge conge);

    Conge toEntity(CongeDTO congeDTO);
}
