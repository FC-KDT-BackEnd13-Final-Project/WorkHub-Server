package com.workhub.projectNotification.api;

import com.workhub.global.response.ApiResponse;
import com.workhub.projectNotification.dto.NotificationResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

@Tag(name = "Notification", description = "프로젝트 알림 API")
@RequestMapping("/api/v1/notifications")
public interface NotificationApi {

    @Operation(summary = "알림 SSE 스트림 구독", description = "Last-Event-ID로 끊긴 알림을 보충하고, 신규 알림을 SSE로 수신합니다.")
    @GetMapping("/stream")
    SseEmitter stream(@RequestHeader(value = "Last-Event-ID", required = false) Long lastEventId);

    @Operation(summary = "알림 목록 조회", description = "최신 알림 목록(예: 최대 50개)을 반환합니다.")
    @GetMapping
    ResponseEntity<ApiResponse<List<NotificationResponse>>> list();

    @Operation(summary = "미읽음 카운트 조회", description = "미읽음 알림 개수를 반환합니다.")
    @GetMapping("/unread-count")
    ResponseEntity<ApiResponse<Long>> unreadCount();

    @Operation(summary = "알림 읽음 처리", description = "지정한 알림을 읽음 상태로 변경합니다.")
    @PatchMapping("/{id}/read")
    ResponseEntity<ApiResponse<Void>> markRead(@PathVariable Long id);
}
