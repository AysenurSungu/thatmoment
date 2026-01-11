package com.thatmoment.modules.profile.service;

import com.thatmoment.modules.profile.dto.request.UpdateUserPreferencesRequest;
import com.thatmoment.modules.profile.dto.response.UserPreferencesResponse;

import java.util.UUID;

public interface UserPreferencesService {

    UserPreferencesResponse getPreferences(UUID userId);

    UserPreferencesResponse updatePreferences(UUID userId, UpdateUserPreferencesRequest request);

    void ensurePreferencesExists(UUID userId);
}
