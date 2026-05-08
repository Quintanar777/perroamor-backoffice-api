package com.perroamor.inventory.shared.error;

public final class NotFoundException extends DomainException {

    public NotFoundException(String message) {
        super(message);
    }

    public static NotFoundException of(String resource, Object id) {
        return new NotFoundException("%s con id %s no encontrado".formatted(resource, id));
    }
}
