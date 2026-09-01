package com.perroamor.inventory.catalog.discounts.infrastructure.web;

import com.perroamor.inventory.catalog.discounts.application.DiscountService;
import com.perroamor.inventory.catalog.discounts.domain.Discount;
import com.perroamor.inventory.catalog.discounts.domain.DiscountFilter;
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
@RequestMapping("/api/v1/discounts")
public class DiscountController {

    private final DiscountService discountService;
    private final DiscountWebMapper mapper;

    public DiscountController(DiscountService discountService, DiscountWebMapper mapper) {
        this.discountService = discountService;
        this.mapper = mapper;
    }

    @GetMapping
    public PagedResponse<DiscountResponse> search(
            @RequestParam(required = false) Boolean isActive,
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        DiscountFilter filter = new DiscountFilter(isActive, q);
        Page<Discount> result = discountService.search(filter, PageRequest.of(page, size));
        return PagedResponse.map(result, mapper::toResponse);
    }

    @GetMapping("/{id}")
    public DiscountResponse getById(@PathVariable Long id) {
        return mapper.toResponse(discountService.getById(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public DiscountResponse create(@Valid @RequestBody DiscountRequest request) {
        Discount created = discountService.create(mapper.toCreateCommand(request));
        return mapper.toResponse(created);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public DiscountResponse update(@PathVariable Long id, @Valid @RequestBody DiscountRequest request) {
        Discount updated = discountService.update(id, mapper.toUpdateCommand(request));
        return mapper.toResponse(updated);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    public void delete(@PathVariable Long id) {
        discountService.delete(id);
    }
}
