package com.workhub.projectNotification.dto;

import com.workhub.projectNotification.entity.NotificationType;
import com.workhub.projectNotification.entity.ProjectNotification;

import java.time.LocalDateTime;

public record NotificationResponse(
        Long id,
        NotificationType type,
        String title,
        String content,
        String relatedUrl,
        Long projectNodeId,
        Long postId,
        Long commentId,
        Long csQnaId,
        boolean read,
        LocalDateTime createdAt
) {
    public static NotificationResponse from(ProjectNotification n) {
        return new NotificationResponse(
                n.getProjectNotificationId(),
                n.getNotificationType(),
                n.getTitle(),
                n.getNotificationContent(),
                n.getRelatedUrl(),
                n.getProjectNodeId(),
                n.getPostId(),
                n.getCommentId(),
                n.getCsQnaId(),
                n.getReadAt() != null,
                n.getCreatedAt()
        );
    }
}
