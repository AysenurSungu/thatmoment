package com.thatmoment.modules.profile.api;

import com.thatmoment.common.constants.ApiDescriptions;
import com.thatmoment.modules.auth.security.UserPrincipal;
import com.thatmoment.modules.profile.dto.request.UpdateUserPreferencesRequest;
import com.thatmoment.modules.profile.dto.request.UpdateUserProfileRequest;
import com.thatmoment.modules.profile.dto.response.UserPreferencesResponse;
import com.thatmoment.modules.profile.dto.response.UserProfileResponse;
import com.thatmoment.modules.profile.service.UserPreferencesService;
import com.thatmoment.modules.profile.service.UserProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/profile")
@Tag(name = ApiDescriptions.TAG_PROFILE, description = ApiDescriptions.TAG_PROFILE_DESC)
@PreAuthorize("isAuthenticated()")
public class UserProfileController {

    private final UserProfileService profileService;
    private final UserPreferencesService preferencesService;

    public UserProfileController(
            UserProfileService profileService,
            UserPreferencesService preferencesService
    ) {
        this.profileService = profileService;
        this.preferencesService = preferencesService;
    }

    @GetMapping
    @Operation(summary = ApiDescriptions.PROFILE_GET_SUMMARY)
    public UserProfileResponse getProfile(@AuthenticationPrincipal UserPrincipal principal) {
        return profileService.getProfile(principal.getUserId());
    }

    @PutMapping
    @Operation(summary = ApiDescriptions.PROFILE_UPDATE_SUMMARY)
    public UserProfileResponse updateProfile(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody UpdateUserProfileRequest request
    ) {
        return profileService.updateProfile(principal.getUserId(), request);
    }

    @GetMapping("/preferences")
    @Operation(summary = ApiDescriptions.PREFERENCES_GET_SUMMARY)
    public UserPreferencesResponse getPreferences(@AuthenticationPrincipal UserPrincipal principal) {
        return preferencesService.getPreferences(principal.getUserId());
    }

    @PutMapping("/preferences")
    @Operation(summary = ApiDescriptions.PREFERENCES_UPDATE_SUMMARY)
    public UserPreferencesResponse updatePreferences(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody UpdateUserPreferencesRequest request
    ) {
        return preferencesService.updatePreferences(principal.getUserId(), request);
    }
}
