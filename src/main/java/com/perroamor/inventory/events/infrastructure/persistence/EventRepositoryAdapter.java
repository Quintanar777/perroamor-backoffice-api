package com.perroamor.inventory.events.infrastructure.persistence;

import com.perroamor.inventory.events.domain.Event;
import com.perroamor.inventory.events.domain.EventFilter;
import com.perroamor.inventory.events.domain.EventRepository;
import com.perroamor.inventory.shared.types.Page;
import com.perroamor.inventory.shared.types.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public class EventRepositoryAdapter implements EventRepository {

    private final EventJpaRepository jpa;
    private final EventMapper mapper;

    public EventRepositoryAdapter(EventJpaRepository jpa, EventMapper mapper) {
        this.jpa = jpa;
        this.mapper = mapper;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Event> search(EventFilter filter, LocalDate today, PageRequest pageRequest) {
        Pageable pageable = org.springframework.data.domain.PageRequest.of(
                pageRequest.page(),
                pageRequest.size(),
                Sort.by(Sort.Order.desc("startDate"), Sort.Order.asc("id")));

        var jpaPage = jpa.findAll(EventSpecifications.withFilter(filter, today), pageable);
        var content = jpaPage.getContent().stream().map(mapper::toDomain).toList();
        return Page.of(content, pageRequest.page(), pageRequest.size(), jpaPage.getTotalElements());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Event> findById(Long id) {
        return jpa.findById(id).map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Event> findCurrent(LocalDate today) {
        return jpa.findCurrent(today).map(mapper::toDomain);
    }

    @Override
    @Transactional
    public Event save(Event event) {
        EventJpaEntity entity = mapper.toEntity(event);
        return mapper.toDomain(jpa.save(entity));
    }

    @Override
    @Transactional
    public Event update(Event event) {
        EventJpaEntity existing = jpa.findById(event.id())
                .orElseThrow(() -> new IllegalStateException("Event " + event.id() + " no encontrado"));
        existing.setName(event.name());
        existing.setLocation(event.location());
        existing.setDescription(event.description());
        existing.setStartDate(event.startDate());
        existing.setEndDate(event.endDate());
        existing.setActive(event.isActive());
        return mapper.toDomain(jpa.saveAndFlush(existing));
    }

    @Override
    @Transactional
    public void softDelete(Long id) {
        jpa.softDelete(id);
    }
}
