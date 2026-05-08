package com.perroamor.inventory.events.infrastructure.web;

import com.perroamor.inventory.events.application.EventService;
import com.perroamor.inventory.events.domain.Event;
import com.perroamor.inventory.events.domain.EventFilter;
import com.perroamor.inventory.events.domain.EventStatus;
import com.perroamor.inventory.shared.types.Page;
import com.perroamor.inventory.shared.types.PageRequest;
import com.perroamor.inventory.shared.types.PagedResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/events")
public class EventController {

    private final EventService eventService;
    private final EventWebMapper mapper;

    public EventController(EventService eventService, EventWebMapper mapper) {
        this.eventService = eventService;
        this.mapper = mapper;
    }

    @GetMapping
    public PagedResponse<EventResponse> search(
            @RequestParam(required = false) EventStatus status,
            @RequestParam(required = false) Boolean isActive,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        EventFilter filter = new EventFilter(status, isActive);
        Page<Event> result = eventService.search(filter, PageRequest.of(page, size));
        return PagedResponse.map(result, mapper::toResponse);
    }

    @GetMapping("/current")
    public EventResponse current() {
        return mapper.toResponse(eventService.getCurrent());
    }

    @GetMapping("/{id}")
    public EventResponse getById(@PathVariable Long id) {
        return mapper.toResponse(eventService.getById(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public EventResponse create(@Valid @RequestBody EventRequest request) {
        Event domain = mapper.toDomain(request);
        return mapper.toResponse(eventService.create(domain));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public EventResponse update(@PathVariable Long id, @Valid @RequestBody EventRequest request) {
        Event domain = mapper.toDomain(request);
        return mapper.toResponse(eventService.update(id, domain));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    public void delete(@PathVariable Long id) {
        eventService.delete(id);
    }
}
