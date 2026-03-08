package com.smart.rh.mapper;

import com.smart.rh.dto.employe.EmployeDto;
import com.smart.rh.dto.employe.EmployeRequest;
import com.smart.rh.entity.Employe;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface EmployeMapper {

    @Mapping(source = "poste.id",    target = "posteId")
    @Mapping(source = "poste.titre", target = "posteTitle")
    @Mapping(source = "recrutement.id", target = "recrutementId")
    @Mapping(source = "user.id",     target = "userId")
    EmployeDto toDto(Employe entity);

    @Mapping(target = "poste",       ignore = true)
    @Mapping(target = "recrutement", ignore = true)
    @Mapping(target = "user",        ignore = true)
    @Mapping(target = "dossier",     ignore = true)
    @Mapping(target = "contrats",    ignore = true)
    @Mapping(target = "plannings",   ignore = true)
    @Mapping(target = "conges",      ignore = true)
    @Mapping(target = "paies",       ignore = true)
    @Mapping(target = "evaluations", ignore = true)
    @Mapping(target = "formations",  ignore = true)
    @Mapping(target = "attendances", ignore = true)
    @Mapping(target = "faceEncoding",ignore = true)
    Employe toEntity(EmployeRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "poste",       ignore = true)
    @Mapping(target = "recrutement", ignore = true)
    @Mapping(target = "user",        ignore = true)
    @Mapping(target = "dossier",     ignore = true)
    @Mapping(target = "contrats",    ignore = true)
    @Mapping(target = "plannings",   ignore = true)
    @Mapping(target = "conges",      ignore = true)
    @Mapping(target = "paies",       ignore = true)
    @Mapping(target = "evaluations", ignore = true)
    @Mapping(target = "formations",  ignore = true)
    @Mapping(target = "attendances", ignore = true)
    @Mapping(target = "faceEncoding",ignore = true)
    void partialUpdate(EmployeRequest request, @MappingTarget Employe entity);

    List<EmployeDto> toDtoList(List<Employe> entities);
}
