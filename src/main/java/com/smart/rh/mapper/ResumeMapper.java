package com.smart.rh.mapper;

import com.smart.rh.dto.recruitment.ResumeDto;
import com.smart.rh.entity.Resume;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ResumeMapper {

    @Mapping(target = "createdAt", ignore = true)
    ResumeDto toDto(Resume entity);

    @Mapping(target = "createdAt", ignore = true)
    Resume toEntity(ResumeDto dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "createdAt", ignore = true)
    void updateEntity(ResumeDto dto, @MappingTarget Resume entity);

    List<ResumeDto> toDtoList(List<Resume> entities);

    List<Resume> toEntityList(List<ResumeDto> dtos);
}
