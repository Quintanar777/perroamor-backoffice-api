package com.perroamor.inventory.events.infrastructure.web;

import com.perroamor.inventory.events.domain.EventStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record EventResponse(
        Long id,
        String name,
        String location,
        String description,
        LocalDate startDate,
        LocalDate endDate,
        boolean isActive,
        EventStatus status,
        long durationDays,
        LocalDateTime createdAt
) {
}
