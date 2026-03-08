package com.smart.rh.mapper;

import com.smart.rh.dto.competence.CompetenceDto;
import com.smart.rh.dto.competence.CompetenceRequest;
import com.smart.rh.entity.Competence;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CompetenceMapper {

    CompetenceDto toDto(Competence entity);

    @Mapping(target = "postes", ignore = true)
    Competence toEntity(CompetenceRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "postes", ignore = true)
    void partialUpdate(CompetenceRequest request, @MappingTarget Competence entity);

    List<CompetenceDto> toDtoList(List<Competence> entities);
}
