package com.perroamor.inventory.shared.types;

public record PageRequest(int page, int size) {

    public PageRequest {
        if (page < 0) {
            throw new IllegalArgumentException("page no puede ser negativo");
        }
        if (size <= 0 || size > 200) {
            throw new IllegalArgumentException("size debe estar entre 1 y 200");
        }
    }

    public static PageRequest of(int page, int size) {
        return new PageRequest(page, size);
    }
}
