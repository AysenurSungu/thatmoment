package com.thatmoment.modules.profile.service.impl;

import com.thatmoment.common.exception.exceptions.NotFoundException;
import com.thatmoment.modules.profile.domain.UserProfile;
import com.thatmoment.modules.profile.dto.request.UpdateUserProfileRequest;
import com.thatmoment.modules.profile.dto.response.UserProfileResponse;
import com.thatmoment.modules.profile.mapper.UserProfileMapper;
import com.thatmoment.modules.profile.repository.UserProfileRepository;
import com.thatmoment.modules.profile.service.UserProfileService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
class UserProfileServiceImpl implements UserProfileService {

    private final UserProfileRepository profileRepository;
    private final UserProfileMapper profileMapper;

    UserProfileServiceImpl(
            UserProfileRepository profileRepository,
            UserProfileMapper profileMapper
    ) {
        this.profileRepository = profileRepository;
        this.profileMapper = profileMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public UserProfileResponse getProfile(UUID userId) {
        UserProfile profile = profileRepository.findByUserIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new NotFoundException("Profile not found"));
        return profileMapper.toResponse(profile);
    }

    @Override
    @Transactional
    public UserProfileResponse updateProfile(UUID userId, UpdateUserProfileRequest request) {
        UserProfile profile = profileRepository.findByUserIdAndDeletedAtIsNull(userId)
                .orElseGet(() -> profileRepository.save(UserProfile.builder().userId(userId).build()));

        profile.updateDetails(
                request.name(),
                request.avatarUrl(),
                request.dateOfBirth()
        );

        return profileMapper.toResponse(profile);
    }

    @Override
    @Transactional
    public void ensureProfileExists(UUID userId) {
        if (!profileRepository.existsByUserIdAndDeletedAtIsNull(userId)) {
            profileRepository.save(UserProfile.builder().userId(userId).build());
        }
    }
}
