package com.thatmoment.modules.profile.mapper;

import com.thatmoment.modules.profile.domain.UserPreferences;
import com.thatmoment.modules.profile.dto.response.NotificationPreferencesResponse;
import com.thatmoment.modules.profile.dto.response.UserPreferencesResponse;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserPreferencesMapper {

    UserPreferencesResponse toResponse(UserPreferences preferences);

    NotificationPreferencesResponse toNotificationResponse(UserPreferences preferences);
}
