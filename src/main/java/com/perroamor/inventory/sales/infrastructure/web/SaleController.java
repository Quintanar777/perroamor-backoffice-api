package com.perroamor.inventory.sales.infrastructure.web;

import com.perroamor.inventory.sales.application.SaleService;
import com.perroamor.inventory.sales.domain.PaymentMethod;
import com.perroamor.inventory.sales.domain.Sale;
import com.perroamor.inventory.sales.domain.SaleFilter;
import com.perroamor.inventory.shared.types.Page;
import com.perroamor.inventory.shared.types.PageRequest;
import com.perroamor.inventory.shared.types.PagedResponse;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/sales")
public class SaleController {

    private final SaleService saleService;
    private final SaleWebMapper mapper;

    public SaleController(SaleService saleService, SaleWebMapper mapper) {
        this.saleService = saleService;
        this.mapper = mapper;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SaleResponse create(@Valid @RequestBody CreateSaleRequest request, Authentication authentication) {
        Sale created = saleService.createSale(mapper.toCommand(request, authentication));
        return mapper.toResponse(created);
    }

    @GetMapping
    public PagedResponse<SaleResponse> search(
            @RequestParam(required = false) Long eventId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @RequestParam(required = false) PaymentMethod paymentMethod,
            @RequestParam(required = false) Boolean includeCancelled,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        SaleFilter filter = new SaleFilter(eventId, from, to, paymentMethod, includeCancelled);
        Page<Sale> result = saleService.search(filter, PageRequest.of(page, size));
        return PagedResponse.map(result, mapper::toResponse);
    }

    @GetMapping("/{id}")
    public SaleResponse getById(@PathVariable Long id) {
        return mapper.toResponse(saleService.getById(id));
    }

    @PatchMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public SaleResponse cancel(@PathVariable Long id) {
        return mapper.toResponse(saleService.cancelSale(id));
    }

    @GetMapping("/stats")
    public SaleStatsResponse stats(@RequestParam Long eventId) {
        return mapper.toStatsResponse(saleService.stats(eventId));
    }
}
