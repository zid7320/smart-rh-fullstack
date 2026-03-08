package com.smart.rh.mapper;

import com.smart.rh.dto.poste.PosteDto;
import com.smart.rh.dto.poste.PosteRequest;
import com.smart.rh.entity.Poste;
import org.mapstruct.*;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PosteMapper {

    @Mapping(target = "competenceIds",  expression = "java(entity.getCompetences().stream().map(c -> c.getId()).collect(java.util.stream.Collectors.toList()))")
    @Mapping(target = "competenceNoms", expression = "java(entity.getCompetences().stream().map(c -> c.getNom()).collect(java.util.stream.Collectors.toList()))")
    PosteDto toDto(Poste entity);

    @Mapping(target = "competences", ignore = true)
    @Mapping(target = "employes",    ignore = true)
    Poste toEntity(PosteRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "competences", ignore = true)
    @Mapping(target = "employes",    ignore = true)
    void partialUpdate(PosteRequest request, @MappingTarget Poste entity);

    List<PosteDto> toDtoList(List<Poste> entities);
}
