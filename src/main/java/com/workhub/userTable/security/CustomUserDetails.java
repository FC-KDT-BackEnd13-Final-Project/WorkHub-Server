package com.workhub.userTable.security;

import com.workhub.userTable.entity.UserTable;

/**
 * Transitional alias for components that still import the old userTable security package.
 * Delegates to the global CustomUserDetails implementation.
 */
public class CustomUserDetails extends com.workhub.global.security.CustomUserDetails {

    public CustomUserDetails(UserTable userTable) {
        super(userTable);
    }
}
