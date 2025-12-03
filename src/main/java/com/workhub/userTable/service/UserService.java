package com.workhub.userTable.service;

import com.workhub.userTable.dto.AdminPasswordResetRequest;
import com.workhub.userTable.dto.UserLoginRecord;
import com.workhub.userTable.dto.UserPasswordChangeRequest;
import com.workhub.userTable.dto.UserRegisterRecord;
import com.workhub.userTable.dto.UserTableResponse;
import com.workhub.userTable.entity.UserRole;
import com.workhub.global.error.ErrorCode;
import com.workhub.global.error.exception.BusinessException;
import com.workhub.userTable.repository.UserRepository;
import com.workhub.userTable.entity.UserTable;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    public UserTable getUserById(Long id) {
        UserTable userTable = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_EXISTS));

        if (userTable.isDeleted()) {
            throw new BusinessException(ErrorCode.USER_NOT_EXISTS);
        }

        return userTable;
    }

    public Authentication login(UserLoginRecord userLoginRecord) {
        UsernamePasswordAuthenticationToken authRequest = new UsernamePasswordAuthenticationToken(
                userLoginRecord.loginId(),
                userLoginRecord.password()
        );

        try {
            return authenticationManager.authenticate(authRequest);
        } catch (AuthenticationException exception) {
            throw new BusinessException(ErrorCode.INVALID_LOGIN_CREDENTIALS);
        }
    }

    @Transactional
    public UserTable register(UserRegisterRecord record) {
        validateLoginId(record.loginId());
        validateEmail(record.email());

        UserTable userTable = UserTable.from(
                record,
                passwordEncoder.encode(record.password())
        );

        return userRepository.save(userTable);
    }

    @Transactional
    public void changePassword(Long userId, UserPasswordChangeRequest request) {
        UserTable userTable = getUserById(userId);

        if (!passwordEncoder.matches(request.currentPassword(), userTable.getPassword())) {
            throw new BusinessException(ErrorCode.INVALID_LOGIN_CREDENTIALS);
        }

        if (passwordEncoder.matches(request.newPassword(), userTable.getPassword())) {
            throw new BusinessException(ErrorCode.INVALID_LOGIN_CREDENTIALS);
        }

        userTable.updatePassword(passwordEncoder.encode(request.newPassword()));
    }

    @Transactional
    public void resetPasswordByAdmin(Long userId, AdminPasswordResetRequest request) {
        UserTable userTable = getUserById(userId);
        userTable.updatePassword(passwordEncoder.encode(request.newPassword()));
    }

    @Transactional
    public UserTableResponse updateRole(Long userId, UserRole role) {
        UserTable userTable = getUserById(userId);
        userTable.updateRole(role);
        return UserTableResponse.from(userTable);
    }

    @Transactional
    public void deleteUser(Long userId) {
        UserTable userTable = getUserById(userId);
        userTable.markDeleted();
    }

    private void validateLoginId(String loginId) {
        if (userRepository.existsByLoginId(loginId)) {
            throw new BusinessException(ErrorCode.ALREADY_REGISTERED_USER);
        }
    }

    private void validateEmail(String email) {
        if (userRepository.existsByEmail(email)) {
            throw new BusinessException(ErrorCode.ALREADY_EXISTS__EMAIL);
        }
    }
}
