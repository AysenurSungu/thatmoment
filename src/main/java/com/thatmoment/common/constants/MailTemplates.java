package com.thatmoment.common.constants;

public final class MailTemplates {

    private MailTemplates() {
    }

    public static final String SUBJECT_EMAIL_VERIFY = "ThatMoment - Email Doğrulama";
    public static final String SUBJECT_LOGIN_OTP = "ThatMoment - Giriş Kodu";
    public static final String SUBJECT_GENERIC = "ThatMoment - Doğrulama Kodu";

    public static final String TEMPLATE_EMAIL_VERIFY = "templates/email-verification.html";
    public static final String TEMPLATE_LOGIN_OTP = "templates/login-otp.html";
    public static final String TEMPLATE_GENERIC = "templates/generic-code.html";
}
