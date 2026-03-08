package com.smart.rh.mapper;

import java.util.List;

/**
 * Base MapStruct mapper contract.
 * All entity-specific mappers should implement this interface.
 *
 * @param <E> JPA Entity
 * @param <D> DTO (response)
 * @param <R> Request DTO (create/update)
 */
public interface BaseMapper<E, D, R> {

    D toDto(E entity);

    E toEntity(R request);

    List<D> toDtoList(List<E> entities);
}
