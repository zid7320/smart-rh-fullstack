package com.smart.rh.mapper;

import com.smart.rh.dto.recrutement.RecrutementDto;
import com.smart.rh.dto.recrutement.RecrutementRequest;
import com.smart.rh.entity.Recrutement;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface RecrutementMapper {

    @Mapping(source = "responsable.id",  target = "responsableId")
    @Mapping(source = "responsable.nom", target = "responsableNom")
    RecrutementDto toDto(Recrutement entity);

    @Mapping(target = "responsable", ignore = true)
    @Mapping(target = "candidates",  ignore = true)
    Recrutement toEntity(RecrutementRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "responsable", ignore = true)
    @Mapping(target = "candidates",  ignore = true)
    void partialUpdate(RecrutementRequest request, @MappingTarget Recrutement entity);

    List<RecrutementDto> toDtoList(List<Recrutement> entities);
}
