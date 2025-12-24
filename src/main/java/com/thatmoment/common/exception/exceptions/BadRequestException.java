package com.thatmoment.common.exception.exceptions;

import com.thatmoment.common.exception.ApiException;
import org.springframework.http.HttpStatus;

public class BadRequestException extends ApiException {

    public BadRequestException(String message) {
        super(message, HttpStatus.BAD_REQUEST, "BAD_REQUEST");
    }
}
