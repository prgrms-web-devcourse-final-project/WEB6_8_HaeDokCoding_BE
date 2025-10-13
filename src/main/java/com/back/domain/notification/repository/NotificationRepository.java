package com.back.domain.notification.repository;

import com.back.domain.notification.entity.Notification;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    @Query("""
        select n from Notification n
         where n.user.id = :userId
         order by n.createdAt desc, n.id desc
    """)
    List<Notification> findMyNotificationsFirstPage(@Param("userId") Long userId,
                                                    Pageable pageable);

    @Query("""
        select n from Notification n
         where n.user.id = :userId
           and (n.createdAt < :lastCreatedAt or (n.createdAt = :lastCreatedAt and n.id < :lastId))
         order by n.createdAt desc, n.id desc
    """)
    List<Notification> findMyNotificationsAfter(@Param("userId") Long userId,
                                                @Param("lastCreatedAt") LocalDateTime lastCreatedAt,
                                                @Param("lastId") Long lastId,
                                                Pageable pageable);

    @Query("""
        select n from Notification n
         where n.id = :id and n.user.id = :userId
    """)
    Notification findByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    long deleteByUser_Id(Long userId);

}
