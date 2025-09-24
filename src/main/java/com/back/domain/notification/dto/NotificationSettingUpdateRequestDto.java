package com.back.domain.notification.dto;

import jakarta.validation.constraints.NotNull;

public record NotificationSettingUpdateRequestDto(
        @NotNull Boolean enabled
) {}

