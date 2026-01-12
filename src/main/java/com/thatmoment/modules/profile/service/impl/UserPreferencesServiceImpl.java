package com.thatmoment.modules.profile.service.impl;

import com.thatmoment.common.exception.exceptions.BadRequestException;
import com.thatmoment.common.exception.exceptions.NotFoundException;
import com.thatmoment.common.exception.exceptions.UnauthorizedException;
import com.thatmoment.modules.profile.constants.ProfileDefaults;
import com.thatmoment.modules.profile.domain.UserPreferences;
import com.thatmoment.modules.profile.dto.request.UpdateUserPreferencesRequest;
import com.thatmoment.modules.profile.dto.response.UserPreferencesResponse;
import com.thatmoment.modules.profile.mapper.UserPreferencesMapper;
import com.thatmoment.modules.profile.repository.UserPreferencesRepository;
import com.thatmoment.modules.profile.service.UserPreferencesService;
import com.thatmoment.modules.profile.constants.ProfileMessages;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.UUID;

@Service
class UserPreferencesServiceImpl implements UserPreferencesService {

    private final UserPreferencesRepository preferencesRepository;
    private final UserPreferencesMapper preferencesMapper;
    private final PasswordEncoder passwordEncoder;

    UserPreferencesServiceImpl(
            UserPreferencesRepository preferencesRepository,
            UserPreferencesMapper preferencesMapper,
            PasswordEncoder passwordEncoder
    ) {
        this.preferencesRepository = preferencesRepository;
        this.preferencesMapper = preferencesMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional(readOnly = true)
    public UserPreferencesResponse getPreferences(UUID userId) {
        UserPreferences preferences = preferencesRepository.findByUserIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new NotFoundException(ProfileMessages.PREFERENCES_NOT_FOUND));
        return preferencesMapper.toResponse(preferences);
    }

    @Override
    @Transactional
    public UserPreferencesResponse updatePreferences(UUID userId, UpdateUserPreferencesRequest request) {
        UserPreferences preferences = preferencesRepository.findByUserIdAndDeletedAtIsNull(userId)
                .orElseGet(() -> preferencesRepository.save(defaultPreferences(userId)));

        preferences.updateDetails(
                request.theme(),
                request.language(),
                request.timezone(),
                request.weekStartDay(),
                request.timeFormat(),
                request.notificationRoutines(),
                request.notificationAchievements(),
                request.notificationStreaks(),
                request.notificationDailyReminder(),
                request.dailyReminderTime()
        );

        return preferencesMapper.toResponse(preferences);
    }

    @Override
    @Transactional
    public UserPreferencesResponse setJournalLock(UUID userId, boolean enabled, String password) {
        UserPreferences preferences = preferencesRepository.findByUserIdAndDeletedAtIsNull(userId)
                .orElseGet(() -> preferencesRepository.save(defaultPreferences(userId)));

        if (enabled) {
            if (password == null || password.isBlank()) {
                throw new BadRequestException(ProfileMessages.JOURNAL_PASSWORD_REQUIRED);
            }
            preferences.setJournalLock(passwordEncoder.encode(password));
        } else {
            preferences.disableJournalLock();
        }

        return preferencesMapper.toResponse(preferences);
    }

    @Override
    @Transactional(readOnly = true)
    public void verifyJournalLock(UUID userId, String password) {
        UserPreferences preferences = preferencesRepository.findByUserIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new NotFoundException(ProfileMessages.PREFERENCES_NOT_FOUND));

        if (!Boolean.TRUE.equals(preferences.getJournalLockEnabled())) {
            throw new BadRequestException(ProfileMessages.JOURNAL_LOCK_NOT_ENABLED);
        }

        if (!passwordEncoder.matches(password, preferences.getJournalPasswordHash())) {
            throw new UnauthorizedException(ProfileMessages.JOURNAL_PASSWORD_INVALID);
        }
    }

    @Override
    @Transactional
    public void changeJournalPassword(UUID userId, String currentPassword, String newPassword) {
        UserPreferences preferences = preferencesRepository.findByUserIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new NotFoundException(ProfileMessages.PREFERENCES_NOT_FOUND));

        if (!Boolean.TRUE.equals(preferences.getJournalLockEnabled())) {
            throw new BadRequestException(ProfileMessages.JOURNAL_LOCK_NOT_ENABLED);
        }

        if (!passwordEncoder.matches(currentPassword, preferences.getJournalPasswordHash())) {
            throw new UnauthorizedException(ProfileMessages.JOURNAL_PASSWORD_INVALID);
        }

        preferences.setJournalLock(passwordEncoder.encode(newPassword));
    }

    @Override
    @Transactional
    public void ensurePreferencesExists(UUID userId) {
        if (!preferencesRepository.existsByUserIdAndDeletedAtIsNull(userId)) {
            preferencesRepository.save(defaultPreferences(userId));
        }
    }

    private UserPreferences defaultPreferences(UUID userId) {
        return UserPreferences.builder()
                .userId(userId)
                .theme(ProfileDefaults.THEME)
                .language(ProfileDefaults.LANGUAGE)
                .timezone(ProfileDefaults.TIMEZONE)
                .weekStartDay(ProfileDefaults.WEEK_START_DAY)
                .timeFormat(ProfileDefaults.TIME_FORMAT)
                .notificationRoutines(Boolean.TRUE)
                .notificationAchievements(Boolean.TRUE)
                .notificationStreaks(Boolean.TRUE)
                .notificationDailyReminder(Boolean.TRUE)
                .dailyReminderTime(ProfileDefaults.DAILY_REMINDER_TIME)
                .journalLockEnabled(ProfileDefaults.JOURNAL_LOCK_ENABLED)
                .build();
    }
}
