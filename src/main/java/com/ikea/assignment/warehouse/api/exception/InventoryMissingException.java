package com.ikea.assignment.warehouse.api.exception;

public class InventoryMissingException extends RuntimeException {

    public InventoryMissingException(String message) {
        super(message);
    }
}
