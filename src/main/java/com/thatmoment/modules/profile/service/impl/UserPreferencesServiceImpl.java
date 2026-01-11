package com.thatmoment.modules.profile.service.impl;

import com.thatmoment.common.exception.exceptions.NotFoundException;
import com.thatmoment.modules.profile.constants.ProfileDefaults;
import com.thatmoment.modules.profile.domain.UserPreferences;
import com.thatmoment.modules.profile.dto.request.UpdateUserPreferencesRequest;
import com.thatmoment.modules.profile.dto.response.UserPreferencesResponse;
import com.thatmoment.modules.profile.mapper.UserPreferencesMapper;
import com.thatmoment.modules.profile.repository.UserPreferencesRepository;
import com.thatmoment.modules.profile.service.UserPreferencesService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
class UserPreferencesServiceImpl implements UserPreferencesService {

    private final UserPreferencesRepository preferencesRepository;
    private final UserPreferencesMapper preferencesMapper;

    UserPreferencesServiceImpl(
            UserPreferencesRepository preferencesRepository,
            UserPreferencesMapper preferencesMapper
    ) {
        this.preferencesRepository = preferencesRepository;
        this.preferencesMapper = preferencesMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public UserPreferencesResponse getPreferences(UUID userId) {
        UserPreferences preferences = preferencesRepository.findByUserIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new NotFoundException("Preferences not found"));
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
                .build();
    }
}
