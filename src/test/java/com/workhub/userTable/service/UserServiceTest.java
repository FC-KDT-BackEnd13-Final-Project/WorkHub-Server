package com.workhub.userTable.service;

import com.workhub.userTable.dto.UserLoginRecord;
import com.workhub.userTable.dto.UserRegisterRecord;
import com.workhub.global.error.ErrorCode;
import com.workhub.global.error.exception.BusinessException;
import com.workhub.userTable.repository.UserRepository;
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
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private UserService userService;

    @Test
    @DisplayName("로그인 성공 시 사용자 정보를 반환한다")
    void login_success() {
        Authentication authentication = mock(Authentication.class);
        given(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).willReturn(authentication);

        Authentication result = userService.login(new UserLoginRecord("admin", "plain-password"));

        assertThat(result).isSameAs(authentication);
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    @DisplayName("존재하지 않는 아이디로 로그인 시 예외가 발생한다")
    void login_userNotFound() {
        given(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .willThrow(new BadCredentialsException("bad"));

        assertThatThrownBy(() -> userService.login(new UserLoginRecord("missing", "pw")))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining(ErrorCode.INVALID_LOGIN_CREDENTIALS.getMessage())
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INVALID_LOGIN_CREDENTIALS);

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    @DisplayName("비밀번호가 일치하지 않으면 예외가 발생한다")
    void login_invalidPassword() {
        given(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .willThrow(new BadCredentialsException("bad"));

        assertThatThrownBy(() -> userService.login(new UserLoginRecord("admin", "wrong")))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INVALID_LOGIN_CREDENTIALS);
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    @DisplayName("ID로 사용자를 조회하면 Long을 Integer로 안전하게 변환한다")
    void getUserById_success() {
        UserTable mockUser = sampleUser();
        given(userRepository.findById(1L)).willReturn(Optional.of(mockUser));

        UserTable found = userService.getUserById(1L);

        assertThat(found).isEqualTo(mockUser);
        verify(userRepository).findById(1L);
    }

    @Test
    @DisplayName("ID로 사용자 조회 시 없으면 예외가 발생한다")
    void getUserById_notFound() {
        given(userRepository.findById(1L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserById(1L))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.USER_NOT_EXISTS);
    }

    @Test
    @DisplayName("관리자가 회원을 등록하면 비밀번호를 암호화해 저장한다")
    void register_success() {
        UserRegisterRecord record = new UserRegisterRecord(
                "newUser",
                "Plain!234",
                "Plain!234",
                "new@workhub.com",
                "01012345678",
                1L,
                Roleenum.CLIENT
        );

        given(userRepository.existsByLoginId("newUser")).willReturn(false);
        given(userRepository.existsByEmail("new@workhub.com")).willReturn(false);
        given(passwordEncoder.encode("Plain!234")).willReturn("encoded-password");
        given(userRepository.save(any(UserTable.class))).willAnswer(invocation -> invocation.getArgument(0));

        UserTable created = userService.register(record);

        assertThat(created.getLoginId()).isEqualTo("newUser");
        assertThat(created.getPassword()).isEqualTo("encoded-password");
        assertThat(created.getRole()).isEqualTo(Roleenum.CLIENT);
        assertThat(created.getStatus()).isEqualTo(Status.ACTIVE);

        verify(passwordEncoder).encode("Plain!234");
        verify(userRepository).save(any(UserTable.class));
    }

    @Test
    @DisplayName("중복된 로그인 아이디로 회원가입 시 예외가 발생한다")
    void register_duplicateLoginId() {
        UserRegisterRecord record = new UserRegisterRecord(
                "duplicate",
                "Plain!234",
                "Plain!234",
                "dup@workhub.com",
                "01012345678",
                1L,
                Roleenum.CLIENT
        );

        given(userRepository.existsByLoginId("duplicate")).willReturn(true);

        assertThatThrownBy(() -> userService.register(record))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.ALREADY_REGISTERED_USER);

        verify(userRepository, never()).save(any(UserTable.class));
    }

    @Test
    @DisplayName("중복된 이메일로 회원가입 시 예외가 발생한다")
    void register_duplicateEmail() {
        UserRegisterRecord record = new UserRegisterRecord(
                "newUser",
                "Plain!234",
                "Plain!234",
                "dup@workhub.com",
                "01012345678",
                1L,
                Roleenum.CLIENT
        );

        given(userRepository.existsByLoginId("newUser")).willReturn(false);
        given(userRepository.existsByEmail("dup@workhub.com")).willReturn(true);

        assertThatThrownBy(() -> userService.register(record))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.ALREADY_EXISTS__EMAIL);

        verify(userRepository, never()).save(any(UserTable.class));
        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    @DisplayName("비밀번호와 확인 비밀번호가 다르면 예외가 발생한다")
    void register_passwordMismatch() {
        UserRegisterRecord record = new UserRegisterRecord(
                "newUser",
                "Plain!234",
                "Different!234",
                "new@workhub.com",
                "01012345678",
                1L,
                Roleenum.CLIENT
        );

        given(userRepository.existsByLoginId("newUser")).willReturn(false);
        given(userRepository.existsByEmail("new@workhub.com")).willReturn(false);

        assertThatThrownBy(() -> userService.register(record))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.NOT_EQUAL_PASSWORD);

        verify(userRepository, never()).save(any(UserTable.class));
        verify(passwordEncoder, never()).encode(anyString());
    }

    private UserTable sampleUser() {
        return UserTable.builder()
                .userId(1L)
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
