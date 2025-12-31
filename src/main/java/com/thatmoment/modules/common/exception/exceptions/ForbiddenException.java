package com.thatmoment.modules.common.exception.exceptions;

import com.thatmoment.modules.common.exception.ApiException;
import org.springframework.http.HttpStatus;

public class ForbiddenException extends ApiException {

    public ForbiddenException(String message) {
        super(message, HttpStatus.FORBIDDEN, "FORBIDDEN");
    }
}
