package com.perroamor.inventory.shared.error;

public final class ConflictException extends DomainException {

    public ConflictException(String message) {
        super(message);
    }
}
