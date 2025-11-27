package com.workhub.userTable.dto;

import com.workhub.userTable.entity.Roleenum;
import com.workhub.userTable.entity.Status;
import com.workhub.userTable.entity.UserTable;

public record UserTableResponse(
        Long userId,
        String loginId,
        String email,
        String phone,
        Roleenum role,
        Status status,
        Long companyId
) {
    public static UserTableResponse from(UserTable userTable) {
        return new UserTableResponse(
                userTable.getUserId(),
                userTable.getLoginId(),
                userTable.getEmail(),
                userTable.getPhone(),
                userTable.getRole(),
                userTable.getStatus(),
                userTable.getCompanyId()
        );
    }
}
