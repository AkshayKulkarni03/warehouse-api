package com.ikea.assignment.warehouse.api.exception.handler;

import com.ikea.assignment.warehouse.api.exception.FileFormatNotSupportedException;
import com.ikea.assignment.warehouse.api.exception.InventoryMissingException;
import com.ikea.assignment.warehouse.api.exception.JsonFileProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@RestControllerAdvice
@Slf4j
public class ApiExceptionHandler {

    @ExceptionHandler(value = {FileFormatNotSupportedException.class, JsonFileProcessingException.class})
    @ResponseStatus(value = BAD_REQUEST)
    public ApiError inputFileProcessingExceptionHandler(RuntimeException ex, WebRequest request) {
        return new ApiError(BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(value = {InventoryMissingException.class})
    @ResponseStatus(value = NOT_FOUND)
    public ApiError inventoryMissingExceptionHandler(InventoryMissingException ex, WebRequest request) {
        return new ApiError(NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(value = INTERNAL_SERVER_ERROR)
    public ApiError globalExceptionHandler(Exception ex, WebRequest request) {
        log.error("Exception details ", ex);
        return new ApiError(INTERNAL_SERVER_ERROR, ex.getMessage());
    }
}
