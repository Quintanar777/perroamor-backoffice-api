package com.perroamor.inventory.shared.error;

public final class ValidationException extends DomainException {

    public ValidationException(String message) {
        super(message);
    }
}
