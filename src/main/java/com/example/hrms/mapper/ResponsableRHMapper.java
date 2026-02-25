package com.example.hrms.mapper;

import com.example.hrms.dto.ResponsableRHDTO;
import com.example.hrms.model.ResponsableRH;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface ResponsableRHMapper {

    ResponsableRHDTO toDTO(ResponsableRH responsableRH);

    ResponsableRH toEntity(ResponsableRHDTO responsableRHDTO);
}
