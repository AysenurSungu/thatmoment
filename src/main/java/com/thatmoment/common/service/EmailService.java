package com.thatmoment.common.service;

import com.thatmoment.common.config.AppMailProperties;
import com.thatmoment.common.constants.MailTemplates;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;
    private final ResourceLoader resourceLoader;
    private final AppMailProperties mailProperties;

    @Async
    public void sendVerificationCode(String to, String code, String purpose) {
        String subject;
        String content;

        switch (purpose) {
            case "EMAIL_VERIFY" -> {
                subject = MailTemplates.SUBJECT_EMAIL_VERIFY;
                content = loadTemplate(MailTemplates.TEMPLATE_EMAIL_VERIFY, code);
            }
            case "LOGIN_OTP" -> {
                subject = MailTemplates.SUBJECT_LOGIN_OTP;
                content = loadTemplate(MailTemplates.TEMPLATE_LOGIN_OTP, code);
            }
            default -> {
                subject = MailTemplates.SUBJECT_GENERIC;
                content = loadTemplate(MailTemplates.TEMPLATE_GENERIC, code);
            }
        }

        sendEmail(to, subject, content);
    }

    private void sendEmail(String to, String subject, String htmlContent) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(mailProperties.getFrom(), mailProperties.getFromName());
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("Email sent to: {}", to);
        } catch (MessagingException | java.io.UnsupportedEncodingException e) {
            log.error("Failed to send email to: {}", to, e);
        }
    }

    private String loadTemplate(String templatePath, String code) {
        Resource resource = resourceLoader.getResource("classpath:" + templatePath);
        try (InputStream inputStream = resource.getInputStream()) {
            String template = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
            return template.formatted(code);
        } catch (IOException e) {
            log.error("Failed to load email template: {}", templatePath, e);
            return "<p>" + code + "</p>";
        }
    }
}
