package com.workhub.userTable.controller;

import com.workhub.global.error.ErrorCode;
import com.workhub.global.error.exception.BusinessException;
import com.workhub.global.response.ApiResponse;
import com.workhub.userTable.dto.EmailVerificationConfirmRequest;
import com.workhub.userTable.dto.EmailVerificationSendRequest;
import com.workhub.userTable.dto.EmailVerificationStatusResponse;
import com.workhub.userTable.service.EmailVerificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/email-verification")
@RequiredArgsConstructor
@Tag(name = "이메일 인증", description = "이메일 인증 코드 발송 및 검증 API")
public class EmailVerificationController {

    private final EmailVerificationService emailVerificationService;

    @PostMapping("/send")
    @Operation(
            summary = "이메일 인증 코드 발송",
            description = "입력한 이메일 주소로 인증 코드를 발송합니다.",
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "발송 성공",
                            content = @Content(schema = @Schema(implementation = ApiResponse.class)))
            }
    )
    public ResponseEntity<ApiResponse<Void>> send(@RequestBody @Valid EmailVerificationSendRequest request) {
        emailVerificationService.sendVerificationCode(request.email(), request.userName());
        return ApiResponse.success(null, "인증 코드가 발송되었습니다.");
    }

    @PostMapping("/confirm")
    @Operation(
            summary = "이메일 인증 코드 확인",
            description = "발송받은 인증 코드를 검증합니다.",
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "검증 성공",
                            content = @Content(schema = @Schema(implementation = ApiResponse.class))),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "검증 실패")
            }
    )
    public ResponseEntity<ApiResponse<EmailVerificationStatusResponse>> confirm(@RequestBody @Valid EmailVerificationConfirmRequest request) {
        boolean verified = emailVerificationService.verifyCode(request.email(), request.code());
        if (!verified) {
            throw new BusinessException(ErrorCode.NOT_EQUAL_CODE);
        }
        return ApiResponse.success(EmailVerificationStatusResponse.of(true), "이메일 인증이 완료되었습니다.");
    }

    @GetMapping("/status")
    @Operation(
            summary = "이메일 인증 여부 조회",
            description = "해당 이메일이 인증되었는지 확인합니다.",
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공",
                            content = @Content(schema = @Schema(implementation = ApiResponse.class)))
            }
    )
    public ResponseEntity<ApiResponse<EmailVerificationStatusResponse>> status(@RequestParam("email") String email) {
        boolean verified = emailVerificationService.isVerified(email);
        return ApiResponse.success(EmailVerificationStatusResponse.of(verified), "이메일 인증 여부가 조회되었습니다.");
    }
}
