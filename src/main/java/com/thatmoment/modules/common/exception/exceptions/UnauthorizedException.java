package com.thatmoment.modules.common.exception.exceptions;

import com.thatmoment.modules.common.exception.ApiException;
import org.springframework.http.HttpStatus;

public class UnauthorizedException extends ApiException {

    public UnauthorizedException(String message) {
        super(message, HttpStatus.UNAUTHORIZED, "UNAUTHORIZED");
    }
}
