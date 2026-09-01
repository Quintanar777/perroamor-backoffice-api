package com.perroamor.inventory.catalog.discounts.infrastructure.persistence;

import com.perroamor.inventory.catalog.infrastructure.persistence.ProductJpaEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;

@Entity
@Table(name = "discount_slot_options")
public class DiscountSlotOptionJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "slot_id", nullable = false)
    private DiscountSlotJpaEntity slot;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private ProductJpaEntity product;

    @Column(name = "final_unit_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal finalUnitPrice;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public DiscountSlotJpaEntity getSlot() { return slot; }
    public void setSlot(DiscountSlotJpaEntity slot) { this.slot = slot; }

    public ProductJpaEntity getProduct() { return product; }
    public void setProduct(ProductJpaEntity product) { this.product = product; }

    public BigDecimal getFinalUnitPrice() { return finalUnitPrice; }
    public void setFinalUnitPrice(BigDecimal finalUnitPrice) { this.finalUnitPrice = finalUnitPrice; }
}
