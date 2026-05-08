package com.perroamor.inventory.catalog.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "products")
public class ProductJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String name;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "brand_id", nullable = false)
    private BrandJpaEntity brand;

    @Column(nullable = false, length = 80)
    private String category;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "wholesale_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal wholesalePrice;

    @Column(nullable = false)
    private int stock;

    @Column(length = 1000)
    private String description;

    @Column(name = "can_be_personalized", nullable = false)
    private boolean canBePersonalized;

    @Column(name = "has_variants", nullable = false)
    private boolean hasVariants;

    @Column(name = "is_active", nullable = false)
    private boolean isActive;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        if (createdAt == null) createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public BrandJpaEntity getBrand() { return brand; }
    public void setBrand(BrandJpaEntity brand) { this.brand = brand; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public BigDecimal getWholesalePrice() { return wholesalePrice; }
    public void setWholesalePrice(BigDecimal wholesalePrice) { this.wholesalePrice = wholesalePrice; }

    public int getStock() { return stock; }
    public void setStock(int stock) { this.stock = stock; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public boolean isCanBePersonalized() { return canBePersonalized; }
    public void setCanBePersonalized(boolean canBePersonalized) { this.canBePersonalized = canBePersonalized; }

    public boolean isHasVariants() { return hasVariants; }
    public void setHasVariants(boolean hasVariants) { this.hasVariants = hasVariants; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { this.isActive = active; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
