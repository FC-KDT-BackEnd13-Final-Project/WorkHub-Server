package com.workhub.userTable.controller;

import com.workhub.global.response.ApiResponse;
import com.workhub.userTable.dto.UserLoginRecord;
import com.workhub.userTable.dto.UserPasswordResetDto;
import com.workhub.userTable.dto.UserRegisterRecord;
import com.workhub.userTable.dto.UserTableResponse;
import com.workhub.userTable.entity.UserTable;
import com.workhub.userTable.security.CustomUserDetails;
import com.workhub.userTable.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.bind.annotation.*;

@Tag(name = "사용자 인증 및 관리", description = "로그인 및 비밀번호 관리 API")
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(
            summary = "사용자 로그인",
            description = "로그인 아이디와 비밀번호를 검증하고 세션을 발급합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "로그인 성공",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "유효하지 않은 요청 본문"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패 (잘못된 자격 증명)"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 오류 (로그인 처리 실패)")
    })
    @PostMapping("/users/login")
    public ApiResponse<String> login(@RequestBody UserLoginRecord userLoginRecord,
                                     HttpServletRequest request) {

        // 서비스에서 실제 인증 (authenticationManager.authenticate 호출)
        Authentication authentication = userService.login(userLoginRecord);

        // SecurityContext 생성해서 Authentication 넣기
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);

        // 세션에 SecurityContext 저장 → 다음 요청에서도 인증 유지
        HttpSession session = request.getSession(true);
        session.setAttribute(
                HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
                context
        );

        return ApiResponse.success("로그인 성공");
    }

    @Operation(
            summary = "관리자 사용자 생성",
            description = "관리자가 신규 사용자 계정을 생성합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "사용자 생성 성공",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = UserTableResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "요청 데이터 검증 실패"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "관리자 권한 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "이미 존재하는 로그인 아이디"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 오류 (사용자 생성 실패)")
    })
    @PostMapping("/admin/users/add/user")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<UserTableResponse> register(@RequestBody @Valid UserRegisterRecord registerRecord) {
        UserTable createdUser = userService.register(registerRecord);
        return ApiResponse.created(UserTableResponse.from(createdUser), "관리자가 계정을 생성했습니다.");
    }

    @Operation(
            summary = "사용자 비밀번호 재설정",
            description = "본인 인증을 통과한 사용자가 새 비밀번호를 설정합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "비밀번호 재설정 성공",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "요청 데이터 검증 실패"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 오류 (비밀번호 재설정 실패)")
    })
    @PatchMapping("/auth/passwordReset/confirm")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<String> updatePassword(@AuthenticationPrincipal CustomUserDetails userDetails,
                                              @Valid @RequestBody UserPasswordResetDto passwordUpdateDto) {
        userService.resetPassword(userDetails.getUserId(), passwordUpdateDto);
        return ApiResponse.success("비밀번호 재설정 완료", "비밀번호 재설정 요청 성공");
    }

    @Operation(
            summary = "관리자 비밀번호 초기화",
            description = "관리자가 지정한 사용자 계정의 비밀번호를 초기화합니다.",
            parameters = {
                    @Parameter(name = "userId", description = "비밀번호를 초기화할 사용자 식별자", in = ParameterIn.PATH, required = true)
            }
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "관리자 비밀번호 초기화 성공",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "요청 데이터 검증 실패"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "관리자 권한 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 오류 (비밀번호 초기화 실패)")
    })
    @PatchMapping("/admin/users/{userId}/password/reset")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<String> resetPasswordByAdmin(@PathVariable Long userId,
                                                    @Valid @RequestBody UserPasswordResetDto passwordResetDto) {
        userService.resetPassword(userId, passwordResetDto);
        return ApiResponse.success("관리자 비밀번호 초기화 완료", "관리자가 비밀번호를 초기화했습니다.");
    }
}
