package com.workhub.dto;

import com.workhub.userTable.entity.Roleenum;
import com.workhub.userTable.entity.Status;
import com.workhub.userTable.entity.UserTable;

public record UserTableRecord(
        Long userId,
        String loginId,
        String email,
        String phone,
        Roleenum role,
        Status status,
        Long companyId
) {
    public static UserTableRecord from(UserTable userTable) {
        return new UserTableRecord(
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
