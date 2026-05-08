package com.perroamor.inventory.events.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record Event(
        Long id,
        String name,
        String location,
        String description,
        LocalDate startDate,
        LocalDate endDate,
        boolean isActive,
        LocalDateTime createdAt
) {

    public EventStatus status(LocalDate today) {
        return EventStatus.from(startDate, endDate, today);
    }
}
