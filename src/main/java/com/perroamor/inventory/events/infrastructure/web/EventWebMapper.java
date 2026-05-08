package com.perroamor.inventory.events.infrastructure.web;

import com.perroamor.inventory.events.domain.Event;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Component
public class EventWebMapper {

    public EventResponse toResponse(Event event) {
        LocalDate today = LocalDate.now();
        long durationDays = ChronoUnit.DAYS.between(event.startDate(), event.endDate()) + 1;
        return new EventResponse(
                event.id(),
                event.name(),
                event.location(),
                event.description(),
                event.startDate(),
                event.endDate(),
                event.isActive(),
                event.status(today),
                durationDays,
                event.createdAt());
    }

    public Event toDomain(EventRequest request) {
        return new Event(
                null,
                request.name(),
                request.location(),
                request.description(),
                request.startDate(),
                request.endDate(),
                request.isActive() == null || request.isActive(),
                null);
    }
}
