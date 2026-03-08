package com.smart.rh.mapper;

import com.smart.rh.dto.recrutement.CandidateDto;
import com.smart.rh.dto.recrutement.CandidateRequest;
import com.smart.rh.entity.Candidate;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CandidateMapper {

    @Mapping(source = "recrutement.id",           target = "recrutementId")
    @Mapping(source = "recrutement.posteCible",   target = "recrutementPosteCible")
    CandidateDto toDto(Candidate entity);

    @Mapping(target = "recrutement", ignore = true)
    Candidate toEntity(CandidateRequest request);

    List<CandidateDto> toDtoList(List<Candidate> entities);
}
