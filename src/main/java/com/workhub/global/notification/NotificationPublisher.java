package com.workhub.global.notification;

import com.workhub.projectNotification.entity.NotificationType;
import com.workhub.projectNotification.service.ProjectNotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.Set;

/**
 * 알림 발행 공통 헬퍼.
 * - 대상이 없으면 아무 것도 하지 않는다.
 * - 연관 FK는 정확히 하나만 세팅해야 DB 제약을 통과한다.
 */
@Component
@RequiredArgsConstructor
public class NotificationPublisher {

    private final ProjectNotificationService notificationService;

    public void publishToUsers(
            Set<Long> receivers,
            NotificationType type,
            String title,
            String content,
            String relatedUrl,
            Long projectId,
            Long projectNodeId,
            Long postId,
            Long commentId,
            Long csQnaId,
            Long csPostId
    ) {
        if (receivers == null || receivers.isEmpty()) {
            return;
        }
        validateExactlyOneRelated(projectId, projectNodeId, postId, commentId, csQnaId, csPostId);

        receivers.forEach(userId ->
                notificationService.publish(
                        userId, type, title, content, relatedUrl,
                        projectNodeId, postId, commentId, csQnaId, projectId, csPostId
                )
        );
    }

    /**
     * DB 체크 제약 대응: 연관 FK는 하나만 세팅되어야 한다.
     */
    private void validateExactlyOneRelated(Object... vals) {
        int count = 0;
        for (Object v : vals) {
            if (Objects.nonNull(v)) {
                count++;
            }
        }
        if (count != 1) {
            throw new IllegalArgumentException("연관 FK는 하나만 설정해야 합니다.");
        }
    }
}
