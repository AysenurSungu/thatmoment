package com.thatmoment.modules.auth.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.thatmoment.common.constants.AuthMessages;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.time.Instant;

@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException
    ) throws IOException {
        String jwtError = (String) request.getAttribute("jwt_error");
        String errorCode;
        String detail;

        if ("TOKEN_EXPIRED".equals(jwtError)) {
            errorCode = "TOKEN_EXPIRED";
            detail = AuthMessages.ACCESS_TOKEN_EXPIRED;
        } else if ("INVALID_TOKEN".equals(jwtError) || "INVALID_TOKEN_TYPE".equals(jwtError)) {
            errorCode = "INVALID_TOKEN";
            detail = AuthMessages.INVALID_ACCESS_TOKEN;
        } else {
            errorCode = "UNAUTHORIZED";
            detail = AuthMessages.AUTHENTICATION_REQUIRED;
        }

        ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.UNAUTHORIZED);
        problemDetail.setType(URI.create("https://thatmoment.com/errors/" + errorCode.toLowerCase().replace("_", "-")));
        problemDetail.setTitle("Unauthorized");
        problemDetail.setDetail(detail);
        problemDetail.setProperty("errorCode", errorCode);
        problemDetail.setProperty("timestamp", Instant.now().toString());

        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(objectMapper.writeValueAsString(problemDetail));
    }
}
