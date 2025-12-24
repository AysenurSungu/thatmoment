package com.thatmoment.common.exception.exceptions;

import com.thatmoment.common.exception.ApiException;
import org.springframework.http.HttpStatus;

public class ForbiddenException extends ApiException {

    public ForbiddenException(String message) {
        super(message, HttpStatus.FORBIDDEN, "FORBIDDEN");
    }
}
