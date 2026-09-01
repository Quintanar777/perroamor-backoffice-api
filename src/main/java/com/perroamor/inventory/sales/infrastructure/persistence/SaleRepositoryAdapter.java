package com.perroamor.inventory.sales.infrastructure.persistence;

import com.perroamor.inventory.auth.infrastructure.persistence.UserJpaEntity;
import com.perroamor.inventory.auth.infrastructure.persistence.UserJpaRepository;
import com.perroamor.inventory.catalog.combos.infrastructure.persistence.ComboJpaEntity;
import com.perroamor.inventory.catalog.combos.infrastructure.persistence.ComboJpaRepository;
import com.perroamor.inventory.catalog.discounts.infrastructure.persistence.DiscountJpaEntity;
import com.perroamor.inventory.catalog.discounts.infrastructure.persistence.DiscountJpaRepository;
import com.perroamor.inventory.catalog.infrastructure.persistence.ProductJpaEntity;
import com.perroamor.inventory.catalog.infrastructure.persistence.ProductJpaRepository;
import com.perroamor.inventory.catalog.infrastructure.persistence.ProductVariantJpaEntity;
import com.perroamor.inventory.catalog.infrastructure.persistence.ProductVariantJpaRepository;
import com.perroamor.inventory.events.infrastructure.persistence.EventJpaEntity;
import com.perroamor.inventory.events.infrastructure.persistence.EventJpaRepository;
import com.perroamor.inventory.sales.domain.PaymentMethod;
import com.perroamor.inventory.sales.domain.Sale;
import com.perroamor.inventory.sales.domain.SaleFilter;
import com.perroamor.inventory.sales.domain.SaleItem;
import com.perroamor.inventory.sales.domain.SaleRepository;
import com.perroamor.inventory.sales.domain.SaleStats;
import com.perroamor.inventory.shared.error.NotFoundException;
import com.perroamor.inventory.shared.types.Page;
import com.perroamor.inventory.shared.types.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public class SaleRepositoryAdapter implements SaleRepository {

    private final SaleJpaRepository jpa;
    private final EventJpaRepository eventJpa;
    private final UserJpaRepository userJpa;
    private final ProductJpaRepository productJpa;
    private final ProductVariantJpaRepository variantJpa;
    private final ComboJpaRepository comboJpa;
    private final DiscountJpaRepository discountJpa;

    public SaleRepositoryAdapter(SaleJpaRepository jpa,
                                 EventJpaRepository eventJpa,
                                 UserJpaRepository userJpa,
                                 ProductJpaRepository productJpa,
                                 ProductVariantJpaRepository variantJpa,
                                 ComboJpaRepository comboJpa,
                                 DiscountJpaRepository discountJpa) {
        this.jpa = jpa;
        this.eventJpa = eventJpa;
        this.userJpa = userJpa;
        this.productJpa = productJpa;
        this.variantJpa = variantJpa;
        this.comboJpa = comboJpa;
        this.discountJpa = discountJpa;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Sale> search(SaleFilter filter, PageRequest pageRequest) {
        Pageable pageable = org.springframework.data.domain.PageRequest.of(
                pageRequest.page(),
                pageRequest.size(),
                Sort.by(Sort.Order.desc("saleDate"), Sort.Order.desc("id")));

        var jpaPage = jpa.findAll(SaleSpecifications.withFilter(filter), pageable);
        var content = jpaPage.getContent().stream().map(SaleMapper::toDomain).toList();
        return Page.of(content, pageRequest.page(), pageRequest.size(), jpaPage.getTotalElements());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Sale> findById(Long id) {
        return jpa.findById(id).map(SaleMapper::toDomain);
    }

    @Override
    @Transactional
    public Sale save(Sale sale) {
        EventJpaEntity event = eventJpa.getReferenceById(sale.eventId());
        UserJpaEntity user = userJpa.getReferenceById(sale.soldByUserId());

        SaleJpaEntity entity = new SaleJpaEntity();
        entity.setEvent(event);
        entity.setSoldBy(user);
        entity.setSaleDate(sale.saleDate());
        entity.setPaymentMethod(sale.paymentMethod());
        entity.setCustomerName(sale.customerName());
        entity.setCustomerPhone(sale.customerPhone());
        entity.setCustomerEmail(sale.customerEmail());
        entity.setNotes(sale.notes());
        entity.setDiscountAmount(sale.discountAmount());
        entity.setTaxAmount(sale.taxAmount());
        entity.setTotalAmount(sale.totalAmount());
        entity.setPaid(sale.isPaid());
        entity.setWholesale(sale.isWholesale());
        entity.setCancelled(sale.isCancelled());
        entity.setCancelledAt(sale.cancelledAt());

        for (SaleItem item : sale.items()) {
            SaleItemJpaEntity itemEntity = new SaleItemJpaEntity();
            if (item.comboId() != null) {
                ComboJpaEntity combo = comboJpa.getReferenceById(item.comboId());
                itemEntity.setCombo(combo);
            } else {
                ProductJpaEntity product = productJpa.getReferenceById(item.productId());
                itemEntity.setProduct(product);
                if (item.variantId() != null) {
                    ProductVariantJpaEntity variant = variantJpa.getReferenceById(item.variantId());
                    itemEntity.setVariant(variant);
                }
            }
            if (item.discountId() != null) {
                DiscountJpaEntity discount = discountJpa.getReferenceById(item.discountId());
                itemEntity.setDiscount(discount);
            }
            itemEntity.setQuantity(item.quantity());
            itemEntity.setUnitPrice(item.unitPrice());
            itemEntity.setPersonalization(item.personalization());
            itemEntity.setLineTotal(item.lineTotal());
            entity.addItem(itemEntity);
        }

        return SaleMapper.toDomain(jpa.saveAndFlush(entity));
    }

    @Override
    @Transactional
    public Sale markCancelled(Long id) {
        SaleJpaEntity entity = jpa.findById(id)
                .orElseThrow(() -> NotFoundException.of("Venta", id));
        entity.setCancelled(true);
        entity.setCancelledAt(LocalDateTime.now());
        return SaleMapper.toDomain(jpa.saveAndFlush(entity));
    }

    @Override
    @Transactional(readOnly = true)
    public SaleStats stats(SaleFilter filter) {
        List<SaleJpaEntity> sales = jpa.findAll(SaleSpecifications.withFilter(filter));

        BigDecimal totalAmount = sales.stream()
                .map(SaleJpaEntity::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<PaymentMethod, List<SaleJpaEntity>> grouped = sales.stream()
                .collect(Collectors.groupingBy(SaleJpaEntity::getPaymentMethod));

        List<SaleStats.PaymentMethodBreakdown> breakdown = grouped.entrySet().stream()
                .map(e -> new SaleStats.PaymentMethodBreakdown(
                        e.getKey(),
                        e.getValue().size(),
                        e.getValue().stream()
                                .map(SaleJpaEntity::getTotalAmount)
                                .reduce(BigDecimal.ZERO, BigDecimal::add)))
                .sorted(Comparator.comparing(b -> b.paymentMethod().name()))
                .toList();

        return new SaleStats(filter.eventId(), sales.size(), totalAmount, breakdown);
    }
}
