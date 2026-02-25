package com.example.hrms.mapper;

import com.example.hrms.dto.PosteDTO;
import com.example.hrms.model.Poste;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface PosteMapper {

    PosteDTO toDTO(Poste poste);

    Poste toEntity(PosteDTO posteDTO);
}
