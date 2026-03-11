package com.finblock.tms.common.exception;

import org.springframework.http.HttpStatus;

public class NotFoundException extends AppException {
    public NotFoundException(String resource, Object id) {
        super("%s with id %s not found".formatted(resource, id), HttpStatus.NOT_FOUND, "NOT_FOUND");
    }
}

