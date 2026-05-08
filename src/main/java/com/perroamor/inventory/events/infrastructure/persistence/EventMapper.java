package com.perroamor.inventory.events.infrastructure.persistence;

import com.perroamor.inventory.events.domain.Event;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface EventMapper {

    @Mapping(target = "isActive", source = "active")
    Event toDomain(EventJpaEntity entity);

    @Mapping(target = "active", source = "isActive")
    @Mapping(target = "createdAt", ignore = true)
    EventJpaEntity toEntity(Event domain);
}
