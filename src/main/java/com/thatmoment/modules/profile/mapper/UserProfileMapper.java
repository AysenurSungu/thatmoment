package com.thatmoment.modules.profile.mapper;

import com.thatmoment.modules.profile.domain.UserProfile;
import com.thatmoment.modules.profile.dto.response.UserProfileResponse;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserProfileMapper {

    UserProfileResponse toResponse(UserProfile profile);
}
