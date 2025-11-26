package com.workhub.userTable.controller;

import com.workhub.dto.UserLoginRecord;
import com.workhub.dto.UserRegisterRecord;
import com.workhub.dto.UserTableRecord;
import com.workhub.global.response.ApiResponse;
import com.workhub.userTable.entity.UserTable;
import com.workhub.userTable.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<String>> login(@RequestBody UserLoginRecord userLoginRecord,
                                                     HttpServletRequest request) {

        UserTable user = userService.login(userLoginRecord);

        UserDetails securityUser = User.withUsername(user.getLoginId())
                .password(user.getPassword())
                .roles(user.getRole().name())
                .build();

        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                securityUser,
                null,
                securityUser.getAuthorities()
        );
        authentication.setDetails(user);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        HttpSession session = request.getSession(true);
        session.setAttribute(
                HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
                SecurityContextHolder.getContext()
        );

        return ResponseEntity.ok(ApiResponse.success("로그인 성공"));
    }

    @PostMapping("/register")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserTableRecord>> register(@RequestBody @Valid UserRegisterRecord registerRecord) {
        UserTable createdUser = userService.register(registerRecord);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(UserTableRecord.from(createdUser), "관리자가 계정을 생성했습니다."));
    }
}
