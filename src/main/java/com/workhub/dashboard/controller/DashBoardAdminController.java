package com.workhub.dashboard.controller;

import com.workhub.dashboard.api.DashBoardAdminApi;
import com.workhub.dashboard.dto.admin.UserCountResponse;
import com.workhub.dashboard.service.admin.DashBoardAdminService;
import com.workhub.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@RequestMapping("/api/v1/admin/dashboard")
public class DashBoardAdminController implements DashBoardAdminApi {

    private final DashBoardAdminService dashBoardAdminService;

    @Override
    @GetMapping("/users/count")
    public ResponseEntity<ApiResponse<UserCountResponse>> getUserCount() {

        UserCountResponse userCount = dashBoardAdminService.getUserCount();

        return ApiResponse.success(userCount, "총 유저가 조회되었습니다.");
    }
}
