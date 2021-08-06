package com.ikea.assignment.warehouse.api.exception.handler;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
@Getter
public class ApiError {

    private final HttpStatus status;
    private final String message;
}
