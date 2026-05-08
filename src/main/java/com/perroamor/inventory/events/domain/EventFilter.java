package com.perroamor.inventory.events.domain;

public record EventFilter(
        EventStatus status,
        Boolean isActive
) {
}
