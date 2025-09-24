package com.back.domain.notification.dto;

import com.back.domain.notification.entity.NotificationSetting;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class NotificationSettingDto {
    private boolean enabled;

    public static NotificationSettingDto from(NotificationSetting s) {
        return new NotificationSettingDto(s.isEnabled());
    }
}

