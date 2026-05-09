package com.perroamor.inventory.catalog.combos.application;

import com.perroamor.inventory.catalog.application.BrandService;
import com.perroamor.inventory.catalog.application.ProductService;
import com.perroamor.inventory.catalog.application.ProductVariantService;
import com.perroamor.inventory.catalog.combos.domain.Combo;
import com.perroamor.inventory.catalog.combos.domain.ComboFilter;
import com.perroamor.inventory.catalog.combos.domain.ComboItem;
import com.perroamor.inventory.catalog.combos.domain.ComboRepository;
import com.perroamor.inventory.catalog.combos.domain.CreateComboCommand;
import com.perroamor.inventory.catalog.combos.domain.UpdateComboCommand;
import com.perroamor.inventory.catalog.domain.Product;
import com.perroamor.inventory.catalog.domain.ProductVariant;
import com.perroamor.inventory.shared.error.NotFoundException;
import com.perroamor.inventory.shared.error.ValidationException;
import com.perroamor.inventory.shared.types.Page;
import com.perroamor.inventory.shared.types.PageRequest;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

    public Combo create(CreateComboCommand command) {
        validatePrices(command.price(), command.wholesalePrice());
        validateItems(command.items());
        brandService.getById(command.brandId());

        List<ComboItem> items = resolveItems(command.items());

        Combo toSave = new Combo(
                null,
                command.name(),
                command.description(),
                command.brandId(),
                null,
                null,
                command.price(),
                command.wholesalePrice(),
                true,
                null,
                items);

        return comboRepository.save(toSave);
    }

    public Combo update(Long id, UpdateComboCommand command) {
        Combo existing = getById(id);
        validatePrices(command.price(), command.wholesalePrice());
        validateItems(command.items());
        brandService.getById(command.brandId());

        List<ComboItem> items = resolveItems(command.items());

        Combo updated = new Combo(
                existing.id(),
                command.name(),
                command.description(),
                command.brandId(),
                null,
                null,
                command.price(),
                command.wholesalePrice(),
                command.isActive(),
                existing.createdAt(),
                items);

        return comboRepository.replace(updated);
    }

    public void delete(Long id) {
        getById(id);
        comboRepository.softDelete(id);
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

    private void validatePrices(java.math.BigDecimal price, java.math.BigDecimal wholesalePrice) {
        if (price == null || price.signum() < 0) {
            throw new ValidationException("El precio del combo no puede ser negativo.");
        }
        if (wholesalePrice == null || wholesalePrice.signum() < 0) {
            throw new ValidationException("El precio de mayoreo del combo no puede ser negativo.");
        }
    }

    private void validateItems(List<CreateComboCommand.NewItem> items) {
        if (items == null || items.isEmpty()) {
            throw new ValidationException("El combo debe tener al menos un componente.");
        }
        Set<String> seen = new HashSet<>();
        for (CreateComboCommand.NewItem item : items) {
            if (item.productId() == null) {
                throw new ValidationException("Cada componente del combo debe tener productId.");
            }
            if (item.quantity() <= 0) {
                throw new ValidationException("La cantidad de cada componente debe ser mayor a cero.");
            }
            String key = item.productId() + ":" + (item.variantId() == null ? "-" : item.variantId());
            if (!seen.add(key)) {
                throw new ValidationException(
                        "El combo tiene componentes duplicados (productId " + item.productId() +
                        (item.variantId() == null ? "" : ", variantId " + item.variantId()) + ").");
            }
        }
    }

    private List<ComboItem> resolveItems(List<CreateComboCommand.NewItem> rawItems) {
        List<ComboItem> resolved = new ArrayList<>(rawItems.size());
        for (CreateComboCommand.NewItem item : rawItems) {
            Product product = productService.getById(item.productId());
            String variantName = null;
            if (item.variantId() != null) {
                ProductVariant variant = variantService.getById(item.variantId());
                if (!variant.productId().equals(item.productId())) {
                    throw new ValidationException(
                            "La variante " + item.variantId() + " no pertenece al producto " + item.productId() + ".");
                }
                variantName = variant.variantName();
            }
            resolved.add(new ComboItem(
                    null,
                    item.productId(),
                    product.name(),
                    item.variantId(),
                    variantName,
                    item.quantity()));
        }
        return resolved;
    }
}
