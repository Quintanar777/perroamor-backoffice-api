package com.perroamor.inventory.catalog.application;

import com.perroamor.inventory.catalog.domain.ProductVariant;
import com.perroamor.inventory.catalog.domain.ProductVariantRepository;
import com.perroamor.inventory.shared.error.ConflictException;
import com.perroamor.inventory.shared.error.NotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductVariantService {

    private final ProductVariantRepository variantRepository;
    private final ProductService productService;

    public ProductVariantService(ProductVariantRepository variantRepository, ProductService productService) {
        this.variantRepository = variantRepository;
        this.productService = productService;
    }

    public List<ProductVariant> listByProduct(Long productId, boolean includeInactive) {
        productService.getById(productId);
        return variantRepository.findByProductId(productId, includeInactive);
    }

    public ProductVariant getById(Long id) {
        return variantRepository.findById(id)
                .orElseThrow(() -> NotFoundException.of("Variante", id));
    }

    public ProductVariant create(Long productId, ProductVariant variant) {
        productService.getById(productId);
        if (variantRepository.existsBySku(variant.sku())) {
            throw new ConflictException("Ya existe una variante con SKU '" + variant.sku() + "'.");
        }
        ProductVariant toSave = new ProductVariant(
                null,
                productId,
                variant.variantName(),
                variant.color(),
                variant.size(),
                variant.design(),
                variant.material(),
                variant.sku(),
                variant.stock(),
                variant.priceAdjustment(),
                true,
                null);
        return variantRepository.save(toSave);
    }

    public ProductVariant update(Long id, ProductVariant variant) {
        ProductVariant existing = getById(id);
        if (variantRepository.existsBySkuAndIdNot(variant.sku(), id)) {
            throw new ConflictException("Ya existe otra variante con SKU '" + variant.sku() + "'.");
        }
        ProductVariant updated = new ProductVariant(
                existing.id(),
                existing.productId(),
                variant.variantName(),
                variant.color(),
                variant.size(),
                variant.design(),
                variant.material(),
                variant.sku(),
                variant.stock(),
                variant.priceAdjustment(),
                variant.isActive(),
                existing.createdAt());
        return variantRepository.update(updated);
    }

    public void delete(Long id) {
        getById(id);
        variantRepository.softDelete(id);
    }

    public ProductVariant decrementStock(Long id, int quantity) {
        return variantRepository.decrementStock(id, quantity);
    }

    public ProductVariant incrementStock(Long id, int quantity) {
        return variantRepository.incrementStock(id, quantity);
    }
}
