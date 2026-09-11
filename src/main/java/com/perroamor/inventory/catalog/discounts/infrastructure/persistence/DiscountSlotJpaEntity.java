package com.perroamor.inventory.catalog.discounts.infrastructure.persistence;

import com.perroamor.inventory.catalog.discounts.domain.SlotType;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "discount_slots")
public class DiscountSlotJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "discount_id", nullable = false)
    private DiscountJpaEntity discount;

    @Column(nullable = false)
    private int position;

    @Enumerated(EnumType.STRING)
    @Column(name = "slot_type", nullable = false, length = 10)
    private SlotType slotType;

    @Column(nullable = false)
    private int quantity;

    @OneToMany(mappedBy = "slot", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("id ASC")
    private List<DiscountSlotOptionJpaEntity> options = new ArrayList<>();

    public void addOption(DiscountSlotOptionJpaEntity option) {
        option.setSlot(this);
        this.options.add(option);
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public DiscountJpaEntity getDiscount() { return discount; }
    public void setDiscount(DiscountJpaEntity discount) { this.discount = discount; }

    public int getPosition() { return position; }
    public void setPosition(int position) { this.position = position; }

    public SlotType getSlotType() { return slotType; }
    public void setSlotType(SlotType slotType) { this.slotType = slotType; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public List<DiscountSlotOptionJpaEntity> getOptions() { return options; }
    public void setOptions(List<DiscountSlotOptionJpaEntity> options) { this.options = options; }
}
