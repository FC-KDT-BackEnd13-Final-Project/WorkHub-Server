package com.workhub.projectNotification.service;

import com.workhub.global.error.ErrorCode;
import com.workhub.global.error.exception.BusinessException;
import com.workhub.projectNotification.dto.NotificationResponse;
import com.workhub.projectNotification.entity.NotificationType;
import com.workhub.projectNotification.entity.ProjectNotification;
import com.workhub.projectNotification.repository.ProjectNotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

/**
 * - 알림을 저장하고 SSE로 전송하는 핵심 서비스.
 * - 멀티 인스턴스 확장 시 Redis Pub/Sub을 publish 지점에서 추가하면 된다.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class ProjectNotificationService {

    private final ProjectNotificationRepository notificationRepository;
    private final NotificationEmitterService emitterService;

    /**
     * 알림 저장 후 SSE로 즉시 푸시.
     * (Redis Pub/Sub 연동 시 여기서 convertAndSend 추가)
     */
    public NotificationResponse publish(Long receiverId, NotificationType type,
                                        String title, String content,
                                        String relatedUrl,
                                        Long projectNodeId, Long postId, Long commentId, Long csQnaId) {
        ProjectNotification saved = notificationRepository.save(
                ProjectNotification.builder()
                        .userId(receiverId)
                        .notificationType(type)
                        .title(title)
                        .notificationContent(content)
                        .relatedUrl(relatedUrl)
                        .projectNodeId(projectNodeId)
                        .postId(postId)
                        .commentId(commentId)
                        .csQnaId(csQnaId)
                        .build()
        );
        NotificationResponse dto = NotificationResponse.from(saved);
        emitterService.send(receiverId, dto); // SSE 푸시
        return dto;
    }

    @Transactional(readOnly = true)
    public List<NotificationResponse> listRecent(Long userId) {
        return notificationRepository.findTop50ByUserIdOrderByProjectNotificationIdDesc(userId)
                .stream().map(NotificationResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public List<NotificationResponse> listAfterId(Long userId, Long lastEventId) {
        return notificationRepository.findByUserIdAndProjectNotificationIdGreaterThanOrderByProjectNotificationIdAsc(userId, lastEventId)
                .stream().map(NotificationResponse::from).toList();
    }

    public void markRead(Long userId, Long id) {
        ProjectNotification n = notificationRepository.findByProjectNotificationIdAndUserId(id, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOTIFICATION_NOT_FOUND));
        n.markRead();
    }

    @Transactional(readOnly = true)
    public long unreadCount(Long userId) {
        return notificationRepository.countByUserIdAndReadAtIsNull(userId);
    }

    /**
     * SSE 구독: emitter 등록 후 lastEventId 이후 알림을 보충 전송한다.
     */
    public SseEmitter subscribe(Long userId, Long lastEventId) {
        SseEmitter emitter = emitterService.subscribe(userId);
        if (lastEventId != null) {
            listAfterId(userId, lastEventId).forEach(dto -> emitterService.send(userId, dto));
        }
        return emitter;
    }
}
