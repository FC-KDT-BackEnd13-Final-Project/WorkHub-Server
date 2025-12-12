package com.workhub.checklist.dto;

import lombok.Builder;

@Builder
public record CheckListCommentRequest(
    String content,
    Long patentClCommentId
) {
}
