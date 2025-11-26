package com.workhub.userTable.service;

import com.workhub.dto.UserLoginRecord;
import com.workhub.global.error.ErrorCode;
import com.workhub.global.error.exception.BusinessException;
import com.workhub.repository.UserRepository;
import com.workhub.userTable.entity.UserTable;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserTable getUserById(Long id) {
        Integer userId = Math.toIntExact(id);
        return userRepository.findById(userId)
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
}
