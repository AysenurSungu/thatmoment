package com.thatmoment.modules.common.constants;

public final class AuthMessages {

    private AuthMessages() {
    }

    public static final String EMAIL_ALREADY_REGISTERED = "Email already registered";
    public static final String USER_NOT_FOUND = "User not found";
    public static final String EMAIL_ALREADY_VERIFIED = "Email already verified";
    public static final String NO_ACTIVE_VERIFICATION_CODE = "No active verification code. Please request a new one.";
    public static final String TOO_MANY_FAILED_ATTEMPTS = "Too many failed attempts. Please request a new code.";
    public static final String INVALID_CODE_REMAINING_FORMAT = "Invalid code. %d attempts remaining.";
    public static final String EMAIL_NOT_VERIFIED = "Email not verified. Please verify your email first.";
    public static final String ACCOUNT_SUSPENDED = "Account is suspended";
    public static final String ACCOUNT_LOCKED = "Account is temporarily locked. Please try again later.";
    public static final String NO_ACTIVE_LOGIN_CODE = "No active login code. Please request a new one.";
    public static final String INVALID_REFRESH_TOKEN = "Invalid refresh token";
    public static final String REFRESH_TOKEN_EXPIRED = "Refresh token expired";
    public static final String TOKEN_REUSE_DETECTED = "Token reuse detected. All sessions revoked.";
    public static final String INVALID_TOKEN_TYPE = "Invalid token type";
    public static final String SESSION_NOT_FOUND_OR_EXPIRED = "Session not found or expired";
    public static final String SESSION_NOT_FOUND = "Session not found";
    public static final String LOGOUT_HEADERS_REQUIRED = "User-Id and Session-Id headers required";
    public static final String AUTHENTICATION_REQUIRED = "Authentication required";
    public static final String ACCESS_TOKEN_EXPIRED = "Access token has expired. Please refresh your token.";
    public static final String INVALID_ACCESS_TOKEN = "Invalid access token.";

    public static final String REGISTRATION_SUCCESS = "Registration successful. Verification code sent.";
    public static final String EMAIL_VERIFIED_SUCCESS = "Email verified successfully";
    public static final String VERIFICATION_CODE_SENT = "Verification code sent";
    public static final String LOGIN_CODE_SENT = "Login code sent to your email";
    public static final String LOGIN_SUCCESS = "Login successful";
    public static final String LOGOUT_SUCCESS = "Logged out successfully";
}
