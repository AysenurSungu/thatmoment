package com.thatmoment.common.exception.exceptions;

import com.thatmoment.common.exception.ApiException;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class TooManyRequestsException extends ApiException {

    private final int retryAfterSeconds;

    public TooManyRequestsException(String message, int retryAfterSeconds) {
        super(message, HttpStatus.TOO_MANY_REQUESTS, "RATE_LIMIT_EXCEEDED");
        this.retryAfterSeconds = retryAfterSeconds;
    }
}
