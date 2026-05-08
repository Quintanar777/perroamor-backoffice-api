package com.perroamor.inventory.events.application;

import com.perroamor.inventory.events.domain.Event;
import com.perroamor.inventory.events.domain.EventFilter;
import com.perroamor.inventory.events.domain.EventRepository;
import com.perroamor.inventory.shared.error.NotFoundException;
import com.perroamor.inventory.shared.error.ValidationException;
import com.perroamor.inventory.shared.types.Page;
import com.perroamor.inventory.shared.types.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class EventService {

    private final EventRepository eventRepository;

    public EventService(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    public Page<Event> search(EventFilter filter, PageRequest pageRequest) {
        return eventRepository.search(filter, LocalDate.now(), pageRequest);
    }

    public Event getById(Long id) {
        return eventRepository.findById(id)
                .orElseThrow(() -> NotFoundException.of("Evento", id));
    }

    public Event getCurrent() {
        return eventRepository.findCurrent(LocalDate.now())
                .orElseThrow(() -> new NotFoundException("No hay un evento en curso."));
    }

    public Event create(Event event) {
        validateDates(event.startDate(), event.endDate());
        Event toSave = new Event(
                null,
                event.name(),
                event.location(),
                event.description(),
                event.startDate(),
                event.endDate(),
                true,
                null);
        return eventRepository.save(toSave);
    }

    public Event update(Long id, Event event) {
        Event existing = getById(id);
        validateDates(event.startDate(), event.endDate());
        Event updated = new Event(
                existing.id(),
                event.name(),
                event.location(),
                event.description(),
                event.startDate(),
                event.endDate(),
                event.isActive(),
                existing.createdAt());
        return eventRepository.update(updated);
    }

    public void delete(Long id) {
        getById(id);
        eventRepository.softDelete(id);
    }

    private void validateDates(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            throw new ValidationException("Las fechas de inicio y fin son obligatorias.");
        }
        if (startDate.isAfter(endDate)) {
            throw new ValidationException("La fecha de inicio no puede ser posterior a la fecha de fin.");
        }
    }
}
