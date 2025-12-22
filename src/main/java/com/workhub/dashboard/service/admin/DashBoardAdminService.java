package com.workhub.dashboard.service.admin;

import com.workhub.dashboard.dto.admin.CompanyCountResponse;
import com.workhub.dashboard.dto.admin.UserCountResponse;
import com.workhub.userTable.service.CompanyService;
import com.workhub.userTable.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashBoardAdminService {

    private final UserService userService;
    private final CompanyService companyService;

    public UserCountResponse getUserCount() {

        return UserCountResponse.from(userService.countActiveUsers());
    }

    public CompanyCountResponse getCompanyCount() {
        return CompanyCountResponse.from(companyService.countActiveCompanies());
    }
}
