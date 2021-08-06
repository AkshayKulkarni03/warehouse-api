package com.ikea.assignment.warehouse.api.exception;

public class FileFormatNotSupportedException extends RuntimeException {

    public FileFormatNotSupportedException(String message) {
        super(message);
    }

    public FileFormatNotSupportedException(String message, Exception ex) {
        super(message, ex);
    }
}
