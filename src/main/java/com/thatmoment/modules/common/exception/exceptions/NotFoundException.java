package com.thatmoment.modules.common.exception.exceptions;

import com.thatmoment.modules.common.exception.ApiException;
import org.springframework.http.HttpStatus;

public class NotFoundException extends ApiException {

    public NotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND, "NOT_FOUND");
    }
}
