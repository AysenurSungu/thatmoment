package com.thatmoment.common.constants;

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
    public static final String PLAN_COMPLETE_SUMMARY = "Complete plan";
    public static final String PLAN_UNCOMPLETE_SUMMARY = "Uncomplete plan";

    public static final String PLAN_CATEGORY_CREATE_SUMMARY = "Create plan category";
    public static final String PLAN_CATEGORY_GET_SUMMARY = "Get plan category";
    public static final String PLAN_CATEGORY_LIST_SUMMARY = "List plan categories";
    public static final String PLAN_CATEGORY_UPDATE_SUMMARY = "Update plan category";
    public static final String PLAN_CATEGORY_DELETE_SUMMARY = "Delete plan category";

    public static final String TAG_JOURNAL = "Journal";
    public static final String TAG_JOURNAL_DESC = "Journal entries and tags";
    public static final String JOURNAL_ENTRY_CREATE_SUMMARY = "Create journal entry";
    public static final String JOURNAL_ENTRY_GET_SUMMARY = "Get journal entry";
    public static final String JOURNAL_ENTRY_LIST_SUMMARY = "List journal entries";
    public static final String JOURNAL_ENTRY_UPDATE_SUMMARY = "Update journal entry";
    public static final String JOURNAL_ENTRY_DELETE_SUMMARY = "Delete journal entry";
    public static final String JOURNAL_TAG_CREATE_SUMMARY = "Create journal tag";
    public static final String JOURNAL_TAG_GET_SUMMARY = "Get journal tag";
    public static final String JOURNAL_TAG_LIST_SUMMARY = "List journal tags";
    public static final String JOURNAL_TAG_UPDATE_SUMMARY = "Update journal tag";
    public static final String JOURNAL_TAG_DELETE_SUMMARY = "Delete journal tag";

    public static final String TAG_PROFILE = "Profile";
    public static final String TAG_PROFILE_DESC = "User profile and preferences";
    public static final String PROFILE_GET_SUMMARY = "Get user profile";
    public static final String PROFILE_UPDATE_SUMMARY = "Update user profile";
    public static final String PREFERENCES_GET_SUMMARY = "Get user preferences";
    public static final String PREFERENCES_UPDATE_SUMMARY = "Update user preferences";
    public static final String JOURNAL_LOCK_UPDATE_SUMMARY = "Enable or disable journal lock";
    public static final String JOURNAL_LOCK_VERIFY_SUMMARY = "Verify journal lock password";
    public static final String JOURNAL_LOCK_PASSWORD_UPDATE_SUMMARY = "Change journal lock password";

    public static final String TAG_ROUTINE = "Routine";
    public static final String TAG_ROUTINE_DESC = "Routine tracking and progress";
    public static final String ROUTINE_CREATE_SUMMARY = "Create routine";
    public static final String ROUTINE_GET_SUMMARY = "Get routine";
    public static final String ROUTINE_LIST_SUMMARY = "List routines";
    public static final String ROUTINE_UPDATE_SUMMARY = "Update routine";
    public static final String ROUTINE_DELETE_SUMMARY = "Delete routine";
    public static final String ROUTINE_ACTIVATE_SUMMARY = "Activate routine";
    public static final String ROUTINE_DEACTIVATE_SUMMARY = "Deactivate routine";
    public static final String ROUTINE_TODAY_SUMMARY = "Today routines";
    public static final String ROUTINE_ACTIVE_SUMMARY = "Active routines";
    public static final String ROUTINE_PROGRESS_CREATE_SUMMARY = "Add routine progress";
    public static final String ROUTINE_PROGRESS_UPDATE_SUMMARY = "Update routine progress";
    public static final String ROUTINE_PROGRESS_LIST_SUMMARY = "List routine progress";
    public static final String ROUTINE_PROGRESS_DELETE_SUMMARY = "Delete routine progress";
    public static final String ROUTINE_SUMMARY_SUMMARY = "Routine summary";
    public static final String ROUTINE_OVERVIEW_SUMMARY = "Routine overview";
    public static final String ROUTINE_REMINDERS_GET_SUMMARY = "Get routine reminders";
    public static final String ROUTINE_REMINDERS_UPDATE_SUMMARY = "Update routine reminders";
    public static final String ROUTINE_SKIP_SUMMARY = "Skip routine";

    public static final String TAG_ANALYTICS = "Analytics";
    public static final String TAG_ANALYTICS_DESC = "Analytics summaries and trends";
    public static final String ANALYTICS_WEEKLY_SUMMARY = "Weekly analytics summary";
    public static final String ANALYTICS_MONTHLY_SUMMARY = "Monthly analytics summary";
    public static final String ANALYTICS_YEARLY_SUMMARY = "Yearly analytics summary";
    public static final String ANALYTICS_PLAN_COMPLETION_SUMMARY = "Plan completion trend";
    public static final String ANALYTICS_JOURNAL_MOOD_SUMMARY = "Journal mood distribution";
    public static final String ANALYTICS_ROUTINE_COMPLETION_SUMMARY = "Routine completion trend";

    public static final String TAG_NOTIFICATION = "Notification";
    public static final String TAG_NOTIFICATION_DESC = "Notification inbox and preferences";
    public static final String NOTIFICATION_LIST_SUMMARY = "List notifications";
    public static final String NOTIFICATION_READ_SUMMARY = "Mark notification read";
    public static final String NOTIFICATION_READ_ALL_SUMMARY = "Mark all notifications read";
    public static final String NOTIFICATION_PREFERENCES_GET_SUMMARY = "Get notification preferences";
    public static final String NOTIFICATION_PREFERENCES_UPDATE_SUMMARY = "Update notification preferences";
}
