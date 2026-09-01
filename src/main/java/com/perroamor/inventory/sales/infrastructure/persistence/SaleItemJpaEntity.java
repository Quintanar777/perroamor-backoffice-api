package com.perroamor.inventory.sales.infrastructure.persistence;

import com.perroamor.inventory.catalog.combos.infrastructure.persistence.ComboJpaEntity;
import com.perroamor.inventory.catalog.discounts.infrastructure.persistence.DiscountJpaEntity;
import com.perroamor.inventory.catalog.infrastructure.persistence.ProductJpaEntity;
import com.perroamor.inventory.catalog.infrastructure.persistence.ProductVariantJpaEntity;
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
@Table(name = "sale_items")
public class SaleItemJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sale_id", nullable = false)
    private SaleJpaEntity sale;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private ProductJpaEntity product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "variant_id")
    private ProductVariantJpaEntity variant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "combo_id")
    private ComboJpaEntity combo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "discount_id")
    private DiscountJpaEntity discount;

    @Column(nullable = false)
    private int quantity;

    @Column(name = "unit_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPrice;

    @Column(length = 255)
    private String personalization;

    @Column(name = "line_total", nullable = false, precision = 12, scale = 2)
    private BigDecimal lineTotal;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public SaleJpaEntity getSale() { return sale; }
    public void setSale(SaleJpaEntity sale) { this.sale = sale; }

    public ProductJpaEntity getProduct() { return product; }
    public void setProduct(ProductJpaEntity product) { this.product = product; }

    public ProductVariantJpaEntity getVariant() { return variant; }
    public void setVariant(ProductVariantJpaEntity variant) { this.variant = variant; }

    public ComboJpaEntity getCombo() { return combo; }
    public void setCombo(ComboJpaEntity combo) { this.combo = combo; }

    public DiscountJpaEntity getDiscount() { return discount; }
    public void setDiscount(DiscountJpaEntity discount) { this.discount = discount; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }

    public String getPersonalization() { return personalization; }
    public void setPersonalization(String personalization) { this.personalization = personalization; }

    public BigDecimal getLineTotal() { return lineTotal; }
    public void setLineTotal(BigDecimal lineTotal) { this.lineTotal = lineTotal; }
}
