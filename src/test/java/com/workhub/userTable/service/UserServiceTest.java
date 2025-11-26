package com.workhub.userTable.service;

import com.workhub.dto.UserLoginRecord;
import com.workhub.global.error.ErrorCode;
import com.workhub.global.error.exception.BusinessException;
import com.workhub.repository.UserRepository;
import com.workhub.userTable.entity.Roleenum;
import com.workhub.userTable.entity.Status;
import com.workhub.userTable.entity.UserTable;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @Test
    @DisplayName("로그인 성공 시 사용자 정보를 반환한다")
    void login_success() {
        UserTable mockUser = sampleUser();
        given(userRepository.findByLoginId("admin")).willReturn(Optional.of(mockUser));
        given(passwordEncoder.matches("plain-password", mockUser.getPassword())).willReturn(true);

        UserTable result = userService.login(new UserLoginRecord("admin", "plain-password"));

        assertThat(result).isSameAs(mockUser);
        verify(userRepository).findByLoginId("admin");
        verify(passwordEncoder).matches("plain-password", mockUser.getPassword());
    }

    @Test
    @DisplayName("존재하지 않는 아이디로 로그인 시 예외가 발생한다")
    void login_userNotFound() {
        given(userRepository.findByLoginId("missing")).willReturn(Optional.empty());

        assertThatThrownBy(() -> userService.login(new UserLoginRecord("missing", "pw")))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining(ErrorCode.INVALID_LOGIN_CREDENTIALS.getMessage())
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INVALID_LOGIN_CREDENTIALS);

        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }

    @Test
    @DisplayName("비밀번호가 일치하지 않으면 예외가 발생한다")
    void login_invalidPassword() {
        UserTable mockUser = sampleUser();
        given(userRepository.findByLoginId("admin")).willReturn(Optional.of(mockUser));
        given(passwordEncoder.matches("wrong", mockUser.getPassword())).willReturn(false);

        assertThatThrownBy(() -> userService.login(new UserLoginRecord("admin", "wrong")))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INVALID_LOGIN_CREDENTIALS);
    }

    @Test
    @DisplayName("ID로 사용자를 조회하면 Long을 Integer로 안전하게 변환한다")
    void getUserById_success() {
        UserTable mockUser = sampleUser();
        given(userRepository.findById(1)).willReturn(Optional.of(mockUser));

        UserTable found = userService.getUserById(1L);

        assertThat(found).isEqualTo(mockUser);
        verify(userRepository).findById(1);
    }

    @Test
    @DisplayName("ID로 사용자 조회 시 없으면 예외가 발생한다")
    void getUserById_notFound() {
        given(userRepository.findById(1)).willReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserById(1L))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.USER_NOT_EXISTS);
    }

    private UserTable sampleUser() {
        return UserTable.builder()
                .userId(1)
                .loginId("admin")
                .password("encoded")
                .email("admin@workhub.com")
                .phone("010-0000-0000")
                .role(Roleenum.ADMIN)
                .status(Status.ACTIVE)
                .companyId(1L)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }
}
