package com.perroamor.inventory.catalog.application;

import com.perroamor.inventory.catalog.domain.Brand;
import com.perroamor.inventory.catalog.domain.BrandRepository;
import com.perroamor.inventory.shared.error.ConflictException;
import com.perroamor.inventory.shared.error.NotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BrandService {

    private final BrandRepository brandRepository;

    public BrandService(BrandRepository brandRepository) {
        this.brandRepository = brandRepository;
    }

    public List<Brand> list(boolean includeInactive) {
        return brandRepository.findAll(includeInactive);
    }

    public Brand getById(Long id) {
        return brandRepository.findById(id)
                .orElseThrow(() -> NotFoundException.of("Marca", id));
    }

    public Brand create(String name, String description, String baseColor) {
        brandRepository.findByName(name).ifPresent(b -> {
            throw new ConflictException("Ya existe una marca con el nombre '" + name + "'.");
        });
        Brand brand = new Brand(null, name, description, baseColor, true, null);
        return brandRepository.save(brand);
    }

    public Brand update(Long id, String name, String description, String baseColor, boolean isActive) {
        Brand existing = getById(id);
        brandRepository.findByName(name).ifPresent(other -> {
            if (!other.id().equals(id)) {
                throw new ConflictException("Ya existe otra marca con el nombre '" + name + "'.");
            }
        });
        Brand updated = new Brand(existing.id(), name, description, baseColor, isActive, existing.createdAt());
        return brandRepository.update(updated);
    }

    public void delete(Long id) {
        getById(id);
        brandRepository.softDelete(id);
    }
}
