package com.perroamor.inventory.catalog.application;

import com.perroamor.inventory.catalog.domain.Product;
import com.perroamor.inventory.catalog.domain.ProductFilter;
import com.perroamor.inventory.catalog.domain.ProductRepository;
import com.perroamor.inventory.shared.error.BusinessRuleException;
import com.perroamor.inventory.shared.error.ConflictException;
import com.perroamor.inventory.shared.error.NotFoundException;
import com.perroamor.inventory.shared.types.Page;
import com.perroamor.inventory.shared.types.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final BrandService brandService;

    public ProductService(ProductRepository productRepository, BrandService brandService) {
        this.productRepository = productRepository;
        this.brandService = brandService;
    }

    public Page<Product> search(ProductFilter filter, PageRequest pageRequest) {
        return productRepository.search(filter, pageRequest);
    }

    public Product getById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> NotFoundException.of("Producto", id));
    }

    public Product create(Product product) {
        brandService.getById(product.brandId());
        if (product.code() != null && productRepository.existsByCode(product.code())) {
            throw new ConflictException("Ya existe un producto con código '" + product.code() + "'.");
        }
        Product toSave = new Product(
                null,
                product.name(),
                product.code(),
                product.brandId(),
                null,
                null,
                product.category(),
                product.price(),
                product.wholesalePrice(),
                product.stock(),
                product.description(),
                product.canBePersonalized(),
                product.hasVariants(),
                true,
                null,
                null);
        Product saved = productRepository.save(toSave);
        if (saved.code() == null) {
            saved = productRepository.update(saved.withCode(generateCode(saved.id())));
        }
        return saved;
    }

    public Product update(Long id, Product product) {
        Product existing = getById(id);
        brandService.getById(product.brandId());
        if (product.code() != null && productRepository.existsByCodeAndIdNot(product.code(), id)) {
            throw new ConflictException("Ya existe otro producto con código '" + product.code() + "'.");
        }
        Product updated = new Product(
                existing.id(),
                product.name(),
                product.code(),
                product.brandId(),
                null,
                null,
                product.category(),
                product.price(),
                product.wholesalePrice(),
                existing.stock(),
                product.description(),
                product.canBePersonalized(),
                product.hasVariants(),
                product.isActive(),
                existing.createdAt(),
                null);
        return productRepository.update(updated);
    }

    public void delete(Long id) {
        getById(id);
        productRepository.softDelete(id);
    }

    public Product adjustStock(Long id, int delta) {
        Product current = getById(id);
        if (current.stock() + delta < 0) {
            throw new BusinessRuleException(
                    "El ajuste deja stock negativo (actual " + current.stock() + ", delta " + delta + ").");
        }
        return productRepository.adjustStock(id, delta);
    }

    public Product setStock(Long id, int newStock) {
        if (newStock < 0) {
            throw new BusinessRuleException("El stock no puede ser negativo.");
        }
        Product current = getById(id);
        int delta = newStock - current.stock();
        if (delta == 0) {
            return current;
        }
        return productRepository.adjustStock(id, delta);
    }

    public Product decrementStock(Long id, int quantity) {
        return productRepository.decrementStock(id, quantity);
    }

    public Product incrementStock(Long id, int quantity) {
        return productRepository.incrementStock(id, quantity);
    }

    public List<Product> backfillMissingCodes() {
        return productRepository.findAllWithoutCode().stream()
                .map(product -> productRepository.update(product.withCode(generateCode(product.id()))))
                .toList();
    }

    private String generateCode(Long id) {
        return "P" + "%06d".formatted(id);
    }
}
