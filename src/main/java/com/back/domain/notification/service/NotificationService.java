package com.back.domain.notification.service;

import com.back.domain.notification.dto.NotificationGoResponseDto;
import com.back.domain.notification.dto.NotificationItemDto;
import com.back.domain.notification.dto.NotificationListResponseDto;
import com.back.domain.notification.entity.Notification;
import com.back.domain.notification.enums.NotificationType;
import com.back.domain.notification.repository.NotificationRepository;
import com.back.domain.post.post.entity.Post;
import com.back.domain.user.entity.User;
import com.back.global.exception.ServiceException;
import com.back.global.rq.Rq;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final Rq rq;

    // 연결을 관리하기 위한 Map (key: userId)
  // ConcurrentHashMap: 멀티스레드 환경에서 컬렉션을 안전하게 사용 가능
    private final Map<Long, SseEmitter> emitters = new ConcurrentHashMap<>();

    // 구독 (클라이언트 연결 유지)
    public SseEmitter subscribe() {
      User user = rq.getActor(); // 현재 로그인한 사용자의 정보 가져오기
      SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
      emitters.put(user.getId(), emitter);

      // 연결 종료 시 제거
      emitter.onCompletion(() -> emitters.remove(user.getId()));
      emitter.onTimeout(() -> emitters.remove(user.getId()));

      return emitter;
    }

    @Transactional(readOnly = true)
    public NotificationListResponseDto getNotifications(Long userId, LocalDateTime lastCreatedAt, Long lastId, int limit) {
        int safeLimit = Math.max(1, Math.min(limit, 100));
        int fetchSize = safeLimit + 1; // 다음 페이지가 있는지 판단하기 위해 1건 더 조회

        List<Notification> rows;
        if (lastCreatedAt == null || lastId == null) {
            rows = notificationRepository.findMyNotificationsFirstPage(userId, PageRequest.of(0, fetchSize));
        } else {
            rows = notificationRepository.findMyNotificationsAfter(userId, lastCreatedAt, lastId, PageRequest.of(0, fetchSize));
        }

        boolean hasNext = rows.size() > safeLimit; // +1 개가 있으면 다음 페이지 존재
        if (hasNext) rows = rows.subList(0, safeLimit);

        List<NotificationItemDto> items = new ArrayList<>();
        for (Notification n : rows) items.add(NotificationItemDto.from(n));

        LocalDateTime nextCreatedAt = null;
        Long nextId = null;
        if (hasNext && !rows.isEmpty()) {
            Notification last = rows.get(rows.size() - 1);
            nextCreatedAt = last.getCreatedAt();
            nextId = last.getId();
        }

        return new NotificationListResponseDto(items, hasNext, nextCreatedAt, nextId);
    }

    // 읽음 처리 + 게시글 링크 반환
    @Transactional
    public NotificationGoResponseDto markAsReadAndGetPostLink(Long userId, Long notificationId) {
        Notification notification = notificationRepository.findByIdAndUserId(notificationId, userId);
        if (notification == null) {
            throw new ServiceException(404, "알림을 찾을 수 없습니다.");
        }
        if (!notification.isRead()) {
            notification.markRead();
        }
        Long postId = notification.getPost().getId();
        String apiUrl = "/api/posts/" + postId;
        return new NotificationGoResponseDto(postId, apiUrl);
    }

    // 알림 생성 및 전송
    @Transactional
    public void sendNotification(User user, Post post, NotificationType type, String message) {
      Notification notification = Notification.builder()
          .user(user)
          .post(post)
          .type(type)
          .message(message)
          .build();

      notificationRepository.save(notification);

      // 실시간 전송
      SseEmitter emitter = emitters.get(user.getId());
      if (emitter != null) {
        try {
          emitter.send(SseEmitter.event()
              .name("notification")
              .data(notification));
        } catch (Exception e) {
          // 전송 실패 시 연결 종료 및 제거
          emitters.remove(user.getId());
        }
      }
    }
}
