package com.back.domain.notification.service;

import com.back.domain.notification.dto.NotificationSettingDto;
import com.back.domain.notification.entity.NotificationSetting;
import com.back.domain.notification.repository.NotificationSettingRepository;
import com.back.domain.user.entity.User;
import com.back.domain.user.repository.UserRepository;
import com.back.global.exception.ServiceException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NotificationSettingService {

    private final NotificationSettingRepository notificationSettingRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public NotificationSettingDto getMySetting(Long userId) {
        NotificationSetting s = notificationSettingRepository.findByUserId(userId);
        if (s == null) {
            // Default when not created yet
            return new NotificationSettingDto(true);
        }
        return NotificationSettingDto.from(s);
    }

    @Transactional
    public NotificationSettingDto setMySetting(Long userId, boolean enabled) {
        NotificationSetting s = notificationSettingRepository.findByUserId(userId);
        if (s == null) {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new ServiceException(404, "사용자를 찾을 수 없습니다."));
            s = NotificationSetting.builder()
                    .user(user)
                    .enabled(enabled)
                    .build();
        } else {
            s.setEnabled(enabled);
        }
        NotificationSetting saved = notificationSettingRepository.save(s);
        return NotificationSettingDto.from(saved);
    }
}
