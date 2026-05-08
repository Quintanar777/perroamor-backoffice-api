package com.perroamor.inventory.catalog.combos.infrastructure.web;

import com.perroamor.inventory.catalog.combos.application.ComboService;
import com.perroamor.inventory.catalog.combos.domain.Combo;
import com.perroamor.inventory.catalog.combos.domain.ComboFilter;
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
@RequestMapping("/api/v1/combos")
public class ComboController {

    private final ComboService comboService;
    private final ComboWebMapper mapper;

    public ComboController(ComboService comboService, ComboWebMapper mapper) {
        this.comboService = comboService;
        this.mapper = mapper;
    }

    @GetMapping
    public PagedResponse<ComboResponse> search(
            @RequestParam(required = false) Long brandId,
            @RequestParam(required = false) Boolean isActive,
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        ComboFilter filter = new ComboFilter(brandId, isActive, q);
        Page<Combo> result = comboService.search(filter, PageRequest.of(page, size));
        return PagedResponse.map(result, mapper::toResponse);
    }

    @GetMapping("/{id}")
    public ComboResponse getById(@PathVariable Long id) {
        return mapper.toResponse(comboService.getById(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ComboResponse create(@Valid @RequestBody ComboRequest request) {
        Combo created = comboService.create(mapper.toCreateCommand(request));
        return mapper.toResponse(created);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ComboResponse update(@PathVariable Long id, @Valid @RequestBody ComboRequest request) {
        Combo updated = comboService.update(id, mapper.toUpdateCommand(request));
        return mapper.toResponse(updated);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    public void delete(@PathVariable Long id) {
        comboService.delete(id);
    }
}
