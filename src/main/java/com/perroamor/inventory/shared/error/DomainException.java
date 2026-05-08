package com.perroamor.inventory.shared.error;

public sealed abstract class DomainException extends RuntimeException
        permits NotFoundException, ConflictException, ValidationException, BusinessRuleException {

    protected DomainException(String message) {
        super(message);
    }

    protected DomainException(String message, Throwable cause) {
        super(message, cause);
    }
}
