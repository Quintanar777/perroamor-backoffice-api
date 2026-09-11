package com.perroamor.inventory.catalog.combos.infrastructure.web;

import com.perroamor.inventory.catalog.combos.application.ComboService;
import com.perroamor.inventory.catalog.combos.domain.Combo;
import com.perroamor.inventory.catalog.combos.domain.ComboFilter;
import com.perroamor.inventory.shared.types.Page;
import com.perroamor.inventory.shared.types.PageRequest;
import com.perroamor.inventory.shared.types.PagedResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * inventory-2-0: combos are retired as a sellable/configurable entity. Only read
 * endpoints remain, kept so historical sale lines that reference a combo can
 * still display its detail. Create/update/delete are intentionally gone (not
 * disabled by role) so clients get a plain 404/405, matching combo-retirement
 * spec's "Combo Write Endpoints Retired" requirement.
 */
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
}
