package com.ikea.assignment.warehouse.api.exception.handler;

import com.ikea.assignment.warehouse.api.exception.FileFormatNotSupportedException;
import com.ikea.assignment.warehouse.api.exception.JsonFileProcessingException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(value = {FileFormatNotSupportedException.class, JsonFileProcessingException.class})
    @ResponseStatus(value = BAD_REQUEST)
    public ApiError gameNotFountException(RuntimeException ex, WebRequest request) {
        return new ApiError(BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(value = INTERNAL_SERVER_ERROR)
    public ApiError globalExceptionHandler(Exception ex, WebRequest request) {
        return new ApiError(INTERNAL_SERVER_ERROR, ex.getMessage());
    }
}
