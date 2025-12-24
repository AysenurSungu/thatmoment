package com.thatmoment.common.exception.exceptions;

import com.thatmoment.common.exception.ApiException;
import org.springframework.http.HttpStatus;

public class NotFoundException extends ApiException {

    public NotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND, "NOT_FOUND");
    }
}
