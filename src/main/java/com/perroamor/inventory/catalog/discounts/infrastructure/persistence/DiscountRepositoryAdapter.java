package com.perroamor.inventory.catalog.discounts.infrastructure.persistence;

import com.perroamor.inventory.catalog.discounts.domain.Discount;
import com.perroamor.inventory.catalog.discounts.domain.DiscountFilter;
import com.perroamor.inventory.catalog.discounts.domain.DiscountRepository;
import com.perroamor.inventory.catalog.discounts.domain.DiscountSlot;
import com.perroamor.inventory.catalog.discounts.domain.DiscountSlotOption;
import com.perroamor.inventory.catalog.infrastructure.persistence.ProductJpaEntity;
import com.perroamor.inventory.catalog.infrastructure.persistence.ProductJpaRepository;
import com.perroamor.inventory.shared.error.NotFoundException;
import com.perroamor.inventory.shared.types.Page;
import com.perroamor.inventory.shared.types.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public class DiscountRepositoryAdapter implements DiscountRepository {

    private final DiscountJpaRepository jpa;
    private final ProductJpaRepository productJpa;

    public DiscountRepositoryAdapter(DiscountJpaRepository jpa, ProductJpaRepository productJpa) {
        this.jpa = jpa;
        this.productJpa = productJpa;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Discount> search(DiscountFilter filter, PageRequest pageRequest) {
        Pageable pageable = org.springframework.data.domain.PageRequest.of(
                pageRequest.page(), pageRequest.size(), Sort.by("id").ascending());

        var jpaPage = jpa.findAll(DiscountSpecifications.withFilter(filter), pageable);
        var content = jpaPage.getContent().stream().map(DiscountMapper::toDomain).toList();
        return Page.of(content, pageRequest.page(), pageRequest.size(), jpaPage.getTotalElements());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Discount> findById(Long id) {
        return jpa.findById(id).map(DiscountMapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Discount> findActive() {
        return jpa.findByIsActiveTrue().stream().map(DiscountMapper::toDomain).toList();
    }

    @Override
    @Transactional
    public Discount save(Discount discount) {
        DiscountJpaEntity entity = new DiscountJpaEntity();
        entity.setName(discount.name());
        entity.setDescription(discount.description());
        entity.setTotalPrice(discount.totalPrice());
        entity.setActive(discount.isActive());

        for (DiscountSlot slot : discount.slots()) {
            entity.addSlot(buildSlotEntity(slot));
        }

        return DiscountMapper.toDomain(jpa.saveAndFlush(entity));
    }

    @Override
    @Transactional
    public Discount replace(Discount discount) {
        DiscountJpaEntity existing = jpa.findById(discount.id())
                .orElseThrow(() -> NotFoundException.of("Descuento", discount.id()));

        existing.setName(discount.name());
        existing.setDescription(discount.description());
        existing.setTotalPrice(discount.totalPrice());
        existing.setActive(discount.isActive());

        // Flush the orphan-removal DELETEs before inserting the new slots: discount_slots
        // has a UNIQUE(discount_id, position) constraint, so without an intermediate flush
        // Hibernate may batch the new-slot INSERTs before the old-slot DELETEs and collide
        // on the same (discount_id, position) pair.
        existing.clearSlots();
        jpa.saveAndFlush(existing);

        for (DiscountSlot slot : discount.slots()) {
            existing.addSlot(buildSlotEntity(slot));
        }

        return DiscountMapper.toDomain(jpa.saveAndFlush(existing));
    }

    @Override
    @Transactional
    public void softDelete(Long id) {
        jpa.softDelete(id);
    }

    private DiscountSlotJpaEntity buildSlotEntity(DiscountSlot slot) {
        DiscountSlotJpaEntity entity = new DiscountSlotJpaEntity();
        entity.setPosition(slot.position());
        entity.setSlotType(slot.slotType());
        entity.setQuantity(slot.quantity());
        for (DiscountSlotOption option : slot.options()) {
            entity.addOption(buildOptionEntity(option));
        }
        return entity;
    }

    private DiscountSlotOptionJpaEntity buildOptionEntity(DiscountSlotOption option) {
        DiscountSlotOptionJpaEntity entity = new DiscountSlotOptionJpaEntity();
        ProductJpaEntity productRef = productJpa.getReferenceById(option.productId());
        entity.setProduct(productRef);
        entity.setFinalUnitPrice(option.finalUnitPrice());
        return entity;
    }
}
