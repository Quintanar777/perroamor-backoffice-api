package com.perroamor.inventory.events.infrastructure.web;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record EventRequest(
        @NotBlank @Size(min = 2, max = 150) String name,
        @NotBlank @Size(min = 2, max = 200) String location,
        @Size(max = 1000) String description,
        @NotNull LocalDate startDate,
        @NotNull LocalDate endDate,
        Boolean isActive
) {
}
