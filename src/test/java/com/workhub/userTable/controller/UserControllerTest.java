package com.workhub.userTable.controller;

import com.workhub.global.response.ApiResponse;
import com.workhub.userTable.dto.UserRoleUpdateRequest;
import com.workhub.userTable.dto.UserTableResponse;
import com.workhub.userTable.entity.Status;
import com.workhub.userTable.entity.UserRole;
import com.workhub.userTable.entity.UserTable;
import com.workhub.userTable.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    @Test
    @DisplayName("관리자가 회원 역할을 성공적으로 변경하면 200 응답을 반환한다")
    void updateUserRole_success() {
        UserTableResponse responseDto = new UserTableResponse(1L, "testUser", "user@test.com", "01012345678",
                UserRole.CLIENT, Status.ACTIVE, 1L);
        when(userService.updateRole(anyLong(), any(UserRole.class))).thenReturn(responseDto);

        ResponseEntity<ApiResponse<UserTableResponse>> response = userController.updateUserRole(
                1L,
                new UserRoleUpdateRequest(UserRole.CLIENT)
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getData().userId()).isEqualTo(1L);
        verify(userService).updateRole(1L, UserRole.CLIENT);
    }

    @Test
    @DisplayName("관리자가 회원을 삭제하면 성공 메시지를 반환한다")
    void deleteUser_success() {
        ResponseEntity<ApiResponse<Object>> response = userController.deleteUser(2L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).isEqualTo("회원이 삭제되었습니다.");
        verify(userService).deleteUser(2L);
    }
}
