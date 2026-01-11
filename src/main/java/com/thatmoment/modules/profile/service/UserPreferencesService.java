package com.thatmoment.modules.profile.service;

import com.thatmoment.modules.profile.dto.request.UpdateUserPreferencesRequest;
import com.thatmoment.modules.profile.dto.response.UserPreferencesResponse;

import java.util.UUID;

public interface UserPreferencesService {

    UserPreferencesResponse getPreferences(UUID userId);

    UserPreferencesResponse updatePreferences(UUID userId, UpdateUserPreferencesRequest request);

    UserPreferencesResponse setJournalLock(UUID userId, boolean enabled, String password);

    void verifyJournalLock(UUID userId, String password);

    void changeJournalPassword(UUID userId, String currentPassword, String newPassword);

    void ensurePreferencesExists(UUID userId);
}
