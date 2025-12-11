package com.workhub.userTable.controller;

import com.workhub.global.response.ApiResponse;
import com.workhub.userTable.dto.PasswordResetConfirmRequest;
import com.workhub.userTable.dto.PasswordResetSendRequest;
import com.workhub.userTable.service.PasswordResetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/password-reset")
@RequiredArgsConstructor
@Tag(name = "비밀번호 찾기", description = "비밀번호 재설정을 위한 이메일 인증 API")
public class PasswordResetController {

    private final PasswordResetService passwordResetService;

    @PostMapping("/send")
    @Operation(
            summary = "비밀번호 재설정 인증 코드 발송",
            description = "가입된 이메일로 비밀번호 재설정에 사용할 인증 코드를 발송합니다.",
            responses = @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "발송 성공",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            )
    )
    public ResponseEntity<ApiResponse<Void>> sendResetCode(@Valid @RequestBody PasswordResetSendRequest request) {
        passwordResetService.sendResetCode(request);
        return ApiResponse.success(null, "비밀번호 재설정 인증 코드가 발송되었습니다.");
    }

    @PostMapping("/confirm")
    @Operation(
            summary = "비밀번호 재설정",
            description = "인증 코드를 검증하고 새로운 비밀번호로 변경합니다.",
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "재설정 성공",
                            content = @Content(schema = @Schema(implementation = ApiResponse.class))
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "401",
                            description = "인증 실패"
                    )
            }
    )
    public ResponseEntity<ApiResponse<Void>> resetPassword(@Valid @RequestBody PasswordResetConfirmRequest request) {
        passwordResetService.resetPassword(request);
        return ApiResponse.success(null, "비밀번호가 재설정되었습니다.");
    }
}
