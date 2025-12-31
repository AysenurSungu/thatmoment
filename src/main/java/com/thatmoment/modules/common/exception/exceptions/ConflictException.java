package com.thatmoment.modules.common.exception.exceptions;

import com.thatmoment.modules.common.exception.ApiException;
import org.springframework.http.HttpStatus;

public class ConflictException extends ApiException {

    public ConflictException(String message) {
        super(message, HttpStatus.CONFLICT, "CONFLICT");
    }
}
