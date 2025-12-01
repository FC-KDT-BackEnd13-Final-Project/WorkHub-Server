package com.workhub.userTable.controller;

import com.workhub.global.response.ApiResponse;
import com.workhub.userTable.api.UserTableApi;
import com.workhub.userTable.dto.UserLoginRecord;
import com.workhub.userTable.dto.UserPasswordResetDto;
import com.workhub.userTable.dto.UserRegisterRecord;
import com.workhub.userTable.dto.UserTableResponse;
import com.workhub.userTable.entity.UserTable;
import com.workhub.global.security.CustomUserDetails;
import com.workhub.userTable.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class UserController implements UserTableApi {

    private final UserService userService;

    @PostMapping("/users/login")
    @Override
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

    @PostMapping("/admin/users/add/user")
    @PreAuthorize("hasRole('ADMIN')")
    @Override
    public ApiResponse<UserTableResponse> register(@RequestBody @Valid UserRegisterRecord registerRecord) {
        UserTable createdUser = userService.register(registerRecord);
        return ApiResponse.created(UserTableResponse.from(createdUser), "관리자가 계정을 생성했습니다.");
    }

    @PatchMapping("/auth/passwordReset/confirm")
    @PreAuthorize("isAuthenticated()")
    @Override
    public ApiResponse<String> updatePassword(@AuthenticationPrincipal CustomUserDetails userDetails,
                                              @Valid @RequestBody UserPasswordResetDto passwordUpdateDto) {
        userService.resetPassword(userDetails.getUserId(), passwordUpdateDto);
        return ApiResponse.success("비밀번호 재설정 완료", "비밀번호 재설정 요청 성공");
    }

    @PatchMapping("/admin/users/{userId}/password/reset")
    @PreAuthorize("hasRole('ADMIN')")
    @Override
    public ApiResponse<String> resetPasswordByAdmin(@PathVariable Long userId,
                                                    @Valid @RequestBody UserPasswordResetDto passwordResetDto) {
        userService.resetPassword(userId, passwordResetDto);
        return ApiResponse.success("관리자 비밀번호 초기화 완료", "관리자가 비밀번호를 초기화했습니다.");
    }
}
