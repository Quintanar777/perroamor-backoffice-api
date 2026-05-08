package com.perroamor.inventory.events.domain;

import java.time.LocalDate;

public enum EventStatus {
    UPCOMING,
    IN_PROGRESS,
    FINISHED;

    public static EventStatus from(LocalDate startDate, LocalDate endDate, LocalDate today) {
        if (today.isBefore(startDate)) return UPCOMING;
        if (today.isAfter(endDate))    return FINISHED;
        return IN_PROGRESS;
    }
}
