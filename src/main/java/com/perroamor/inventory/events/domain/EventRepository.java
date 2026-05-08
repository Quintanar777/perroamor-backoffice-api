package com.perroamor.inventory.events.domain;

import com.perroamor.inventory.shared.types.Page;
import com.perroamor.inventory.shared.types.PageRequest;

import java.time.LocalDate;
import java.util.Optional;

public interface EventRepository {

    Page<Event> search(EventFilter filter, LocalDate today, PageRequest pageRequest);

    Optional<Event> findById(Long id);

    Optional<Event> findCurrent(LocalDate today);

    Event save(Event event);

    Event update(Event event);

    void softDelete(Long id);
}
