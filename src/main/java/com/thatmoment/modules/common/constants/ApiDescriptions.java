package com.thatmoment.modules.common.constants;

public final class ApiDescriptions {

    private ApiDescriptions() {
    }

    public static final String TAG_HEALTH = "Health";
    public static final String TAG_HEALTH_DESC = "Service health and readiness checks";
    public static final String HEALTH_SUMMARY = "Health check";
    public static final String HEALTH_DESCRIPTION = "Returns basic service status and timestamp for uptime monitoring.";

    public static final String TAG_AUTH = "Auth";
    public static final String TAG_AUTH_DESC = "Authentication and verification endpoints";
    public static final String REGISTER_SUMMARY = "Register user by email";
    public static final String REGISTER_DESCRIPTION = "Creates a user account and issues an email verification code.";
    public static final String VERIFY_EMAIL_SUMMARY = "Verify email";
    public static final String VERIFY_EMAIL_DESCRIPTION = "Confirms the email with the verification code.";
    public static final String RESEND_CODE_SUMMARY = "Resend verification code";
    public static final String RESEND_CODE_DESCRIPTION = "Sends a new email verification code.";
    public static final String LOGIN_SUMMARY = "Request login code";
    public static final String LOGIN_DESCRIPTION = "Sends a login OTP to the user's email.";
    public static final String LOGIN_VERIFY_SUMMARY = "Verify login code";
    public static final String LOGIN_VERIFY_DESCRIPTION = "Validates the login OTP and signs the user in.";
    public static final String REFRESH_SUMMARY = "Refresh tokens";
    public static final String REFRESH_DESCRIPTION = "Rotates refresh token and issues a new access token.";
    public static final String LOGOUT_SUMMARY = "Logout";
    public static final String LOGOUT_DESCRIPTION = "Revokes the current session or all sessions.";
    public static final String SESSIONS_SUMMARY = "Active sessions";
    public static final String SESSIONS_DESCRIPTION = "Returns active sessions for the current user.";

    public static final String TAG_PLAN = "Plan";
    public static final String TAG_PLAN_DESC = "Plan management endpoints";
    public static final String PLAN_CREATE_SUMMARY = "Create plan";
    public static final String PLAN_GET_SUMMARY = "Get plan";
    public static final String PLAN_LIST_SUMMARY = "List plans";
    public static final String PLAN_UPDATE_SUMMARY = "Update plan";
    public static final String PLAN_DELETE_SUMMARY = "Delete plan";

    public static final String PLAN_CATEGORY_CREATE_SUMMARY = "Create plan category";
    public static final String PLAN_CATEGORY_GET_SUMMARY = "Get plan category";
    public static final String PLAN_CATEGORY_LIST_SUMMARY = "List plan categories";
    public static final String PLAN_CATEGORY_UPDATE_SUMMARY = "Update plan category";
    public static final String PLAN_CATEGORY_DELETE_SUMMARY = "Delete plan category";
}
