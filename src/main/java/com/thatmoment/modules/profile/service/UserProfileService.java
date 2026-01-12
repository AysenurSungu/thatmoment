package com.thatmoment.modules.profile.service;

import com.thatmoment.modules.profile.dto.request.UpdateUserProfileRequest;
import com.thatmoment.modules.profile.dto.response.UserProfileResponse;

import java.util.UUID;

public interface UserProfileService {

    UserProfileResponse getProfile(UUID userId);

    UserProfileResponse updateProfile(UUID userId, UpdateUserProfileRequest request);

    void ensureProfileExists(UUID userId);
}
