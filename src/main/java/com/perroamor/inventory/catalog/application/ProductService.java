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

import java.text.Normalizer;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ThreadLocalRandom;

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
                product.size(),
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
            saved = productRepository.update(saved.withCode(generateCode(saved)));
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
                product.size(),
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
                .map(product -> productRepository.update(product.withCode(generateCode(product))))
                .toList();
    }

    // Si el producto todavía no tiene código, "regenerar" es en realidad la
    // primera generación: se usa el esquema determinístico (5 letras + talla)
    // para que quede legible desde el arranque, igual que create()/
    // backfillMissingCodes(). Si ya tenía un código y se pide reemplazarlo (ej.
    // etiqueta dañada o código comprometido), se usa uno aleatorio -- un código
    // determinístico volvería a dar el mismo resultado (o un sufijo -2, -3...
    // creciendo en cada click) porque nombre/talla no cambiaron.
    public Product regenerateCode(Long id) {
        Product product = getById(id);
        if (product.code() == null) {
            return productRepository.update(product.withCode(generateCode(product)));
        }
        String code = generateRandomCode();
        int attempts = 0;
        while (productRepository.existsByCode(code) && attempts < 5) {
            code = generateRandomCode();
            attempts++;
        }
        return productRepository.update(product.withCode(code));
    }

    private static final int NAME_PREFIX_LENGTH = 5;

    // Código = primeras 5 letras del nombre (sin espacios ni acentos) + "-" +
    // TALLA, en mayúsculas (ej. "Collar Milagro" + "S" -> "COLLA-S"). Corto a
    // propósito -- es solo el punto de partida, se puede editar a mano desde
    // el formulario de producto. Si el resultado ya existe (mismo prefijo+
    // talla en dos productos), se agrega un sufijo numérico hasta encontrar
    // uno libre.
    private String generateCode(Product product) {
        String letters = normalizeCodeSegment(product.name()).replace("-", "");
        String base = letters.length() > NAME_PREFIX_LENGTH ? letters.substring(0, NAME_PREFIX_LENGTH) : letters;
        String sizePart = normalizeCodeSegment(product.size());
        if (!sizePart.isBlank()) {
            base = base + "-" + sizePart;
        }
        String candidate = base;
        int suffix = 2;
        while (productRepository.existsByCode(candidate)) {
            candidate = base + "-" + suffix;
            suffix++;
        }
        return candidate;
    }

    private String normalizeCodeSegment(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }
        String withoutAccents = Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        return withoutAccents.toUpperCase(Locale.ROOT)
                .replaceAll("[^A-Z0-9]+", "-")
                .replaceAll("^-+|-+$", "");
    }

    // Sin 0/O ni 1/I/L — se evitan caracteres ambiguos porque el código
    // también sirve como fallback tecleado a mano si el lector no está a la mano.
    private static final String RANDOM_CODE_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";

    private String generateRandomCode() {
        var random = ThreadLocalRandom.current();
        var sb = new StringBuilder("P");
        for (int i = 0; i < 6; i++) {
            sb.append(RANDOM_CODE_CHARS.charAt(random.nextInt(RANDOM_CODE_CHARS.length())));
        }
        return sb.toString();
    }
}
