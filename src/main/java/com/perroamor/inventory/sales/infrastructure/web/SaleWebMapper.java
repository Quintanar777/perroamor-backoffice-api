package com.perroamor.inventory.sales.infrastructure.web;

import com.perroamor.inventory.auth.domain.UserRepository;
import com.perroamor.inventory.sales.domain.CreateSaleCommand;
import com.perroamor.inventory.sales.domain.Sale;
import com.perroamor.inventory.sales.domain.SaleItem;
import com.perroamor.inventory.sales.domain.SaleStats;
import com.perroamor.inventory.shared.error.ValidationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

@Component
public class SaleWebMapper {

    private final UserRepository userRepository;

    public SaleWebMapper(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public CreateSaleCommand toCommand(CreateSaleRequest request, Authentication authentication) {
        Long userId = extractUserId(authentication);
        return new CreateSaleCommand(
                request.eventId(),
                userId,
                request.paymentMethod(),
                request.customerName(),
                request.customerPhone(),
                request.customerEmail(),
                request.notes(),
                request.discountAmount(),
                request.taxAmount(),
                request.isPaid() == null || request.isPaid(),
                request.items().stream()
                        .map(i -> new CreateSaleCommand.NewItem(
                                i.productId(), i.variantId(), i.comboId(), i.quantity(),
                                i.unitPrice(), i.personalization()))
                        .toList());
    }

    public SaleResponse toResponse(Sale sale) {
        return new SaleResponse(
                sale.id(),
                sale.eventId(),
                sale.soldByUserId(),
                sale.soldByUsername(),
                sale.saleDate(),
                sale.paymentMethod(),
                sale.customerName(),
                sale.customerPhone(),
                sale.customerEmail(),
                sale.notes(),
                sale.discountAmount(),
                sale.taxAmount(),
                sale.totalAmount(),
                sale.subtotal(),
                sale.isPaid(),
                sale.isCancelled(),
                sale.cancelledAt(),
                sale.createdAt(),
                sale.items().stream().map(this::toItemResponse).toList());
    }

    private SaleItemResponse toItemResponse(SaleItem item) {
        return new SaleItemResponse(
                item.id(),
                item.productId(),
                item.productName(),
                item.variantId(),
                item.variantName(),
                item.comboId(),
                item.comboName(),
                item.quantity(),
                item.unitPrice(),
                item.personalization(),
                item.lineTotal());
    }

    public SaleStatsResponse toStatsResponse(SaleStats stats) {
        return new SaleStatsResponse(
                stats.eventId(),
                stats.totalSales(),
                stats.totalAmount(),
                stats.byPaymentMethod().stream()
                        .map(b -> new SaleStatsResponse.PaymentMethodBreakdown(
                                b.paymentMethod(), b.count(), b.amount()))
                        .toList());
    }

    private Long extractUserId(Authentication authentication) {
        if (!(authentication instanceof JwtAuthenticationToken jwtAuth)) {
            throw new ValidationException("Sesión inválida.");
        }
        Jwt jwt = jwtAuth.getToken();
        Object raw = jwt.getClaim("uid");
        if (raw instanceof Number n) {
            return n.longValue();
        }
        // Fallback: cargar por username si el claim uid no está presente.
        String username = jwt.getSubject();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ValidationException("Usuario autenticado no encontrado."))
                .id();
    }
}
