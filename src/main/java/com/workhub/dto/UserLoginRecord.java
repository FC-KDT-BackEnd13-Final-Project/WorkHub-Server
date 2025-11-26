package com.workhub.dto;

import com.workhub.userTable.entity.UserTable;

public record UserLoginRecord(
        String loginId,
        String password
){
        public static LoginRequest from(UserTable userTable) {
            return new LoginRequest(
                    userTable.getLoginId(),
                    userTable.getPassword()
            );
        }
    }
