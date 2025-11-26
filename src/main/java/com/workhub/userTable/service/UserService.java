package com.workhub.userTable.service;

import com.workhub.dto.UserLoginRecord;
import com.workhub.dto.UserRegisterRecord;
import com.workhub.global.error.ErrorCode;
import com.workhub.global.error.exception.BusinessException;
import com.workhub.repository.UserRepository;
import com.workhub.userTable.entity.Status;
import com.workhub.userTable.entity.UserTable;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserTable getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_EXISTS));
    }

    public UserTable login(UserLoginRecord userLoginRecord) {
        UserTable userTable = userRepository.findByLoginId(userLoginRecord.loginId())
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_LOGIN_CREDENTIALS));

        if (!passwordEncoder.matches(userLoginRecord.password(), userTable.getPassword())) {
            throw new BusinessException(ErrorCode.INVALID_LOGIN_CREDENTIALS);
        }

        return userTable;
    }

    @Transactional
    public UserTable register(UserRegisterRecord record) {
        validateLoginId(record.loginId());
        validateEmail(record.email());

        if (!record.password().equals(record.confirmPassword())) {
            throw new BusinessException(ErrorCode.NOT_EQUAL_PASSWORD);
        }

        UserTable userTable = UserTable.builder()
                .loginId(record.loginId())
                .password(passwordEncoder.encode(record.password()))
                .email(record.email())
                .phone(record.phone())
                .role(record.role())
                .status(Status.ACTIVE)
                .companyId(record.companyId())
                .build();

        return userRepository.save(userTable);
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
