package com.thatmoment.modules.auth.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LogoutRequest {

    private String sessionId;
    private boolean allDevices;
}
