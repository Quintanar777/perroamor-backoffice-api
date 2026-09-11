package com.perroamor.inventory.catalog.combos.application;

import com.perroamor.inventory.catalog.application.BrandService;
import com.perroamor.inventory.catalog.application.ProductService;
import com.perroamor.inventory.catalog.application.ProductVariantService;
import com.perroamor.inventory.catalog.combos.domain.Combo;
import com.perroamor.inventory.catalog.combos.domain.ComboFilter;
import com.perroamor.inventory.catalog.combos.domain.ComboItem;
import com.perroamor.inventory.catalog.combos.domain.ComboRepository;
import com.perroamor.inventory.catalog.domain.Product;
import com.perroamor.inventory.catalog.domain.ProductVariant;
import com.perroamor.inventory.shared.error.NotFoundException;
import com.perroamor.inventory.shared.types.Page;
import com.perroamor.inventory.shared.types.PageRequest;
import org.springframework.stereotype.Service;

/**
 * inventory-2-0: combo write paths (create/update/delete) are retired. This
 * service is now read-only + derived availableStock, kept for historical sale
 * display and cancellation stock restitution (see combo-retirement spec).
 * {@code brandService} is kept as a constructor dependency even though it is
 * currently unused by the remaining methods, to avoid an unnecessary breaking
 * change to Spring wiring / test fixtures for a component with no other callers.
 */
@Service
public class ComboService {

    private final ComboRepository comboRepository;
    private final BrandService brandService;
    private final ProductService productService;
    private final ProductVariantService variantService;

    public ComboService(ComboRepository comboRepository,
                        BrandService brandService,
                        ProductService productService,
                        ProductVariantService variantService) {
        this.comboRepository = comboRepository;
        this.brandService = brandService;
        this.productService = productService;
        this.variantService = variantService;
    }

    public Page<Combo> search(ComboFilter filter, PageRequest pageRequest) {
        return comboRepository.search(filter, pageRequest);
    }

    public Combo getById(Long id) {
        return comboRepository.findById(id)
                .orElseThrow(() -> NotFoundException.of("Combo", id));
    }

    /**
     * Stock disponible derivado: cuántas unidades del combo se pueden vender,
     * limitado por el componente con menor stock relativo a su qty requerida.
     */
    public int availableStock(Combo combo) {
        if (combo.items().isEmpty()) {
            return 0;
        }
        int min = Integer.MAX_VALUE;
        for (ComboItem item : combo.items()) {
            int componentStock;
            if (item.variantId() != null) {
                ProductVariant variant = variantService.getById(item.variantId());
                componentStock = variant.stock();
            } else {
                Product product = productService.getById(item.productId());
                componentStock = product.stock();
            }
            int unitsFromComponent = componentStock / item.quantity();
            if (unitsFromComponent < min) {
                min = unitsFromComponent;
            }
        }
        return min == Integer.MAX_VALUE ? 0 : min;
    }
}
