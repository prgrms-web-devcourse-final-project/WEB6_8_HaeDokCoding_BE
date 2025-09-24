package com.back.domain.notification.repository;

import com.back.domain.notification.entity.NotificationSetting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationSettingRepository extends JpaRepository<NotificationSetting, Long> {

    @Query("""
        select ns from NotificationSetting ns
         where ns.user.id = :userId
    """)
    NotificationSetting findByUserId(@Param("userId") Long userId);
}

