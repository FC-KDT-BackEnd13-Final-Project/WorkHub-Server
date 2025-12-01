package com.workhub.userTable.entity;

import com.workhub.global.entity.BaseTimeEntity;
import com.workhub.userTable.dto.UserRegisterRecord;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_table")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UserTable extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "login_id", nullable = false, length = 30)
    private String loginId;

    @Column(name = "password", nullable = false, length = 255)
    private String password;

    @Column(name = "email", nullable = false, length = 50)
    private String email;

    @Column(name = "phone", nullable = false, length = 12)
    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(name = "user_role", nullable = false)
    private UserRole role;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private Status status;

    @Column(name = "lasted_at")
    private LocalDateTime lastedAt;

    @Column(name = "company_id", nullable = false)
    private Long companyId;

    public void updatePassword(String encodedPassword) {
        this.password = encodedPassword;
        this.lastedAt = LocalDateTime.now();
    }

    public static UserTable of(
            String loginId,
            String encodedPassword,
            String email,
            String phone,
            UserRole role,
            Status status,
            Long companyId
    ) {
        UserTable userTable = new UserTable();
        userTable.loginId = loginId;
        userTable.password = encodedPassword;
        userTable.email = email;
        userTable.phone = phone;
        userTable.role = role;
        userTable.status = status;
        userTable.companyId = companyId;
        return userTable;
    }

    public static UserTable from(UserRegisterRecord registerRecord, String encodedPassword) {
        return UserTable.of(
                registerRecord.loginId(),
                encodedPassword,
                registerRecord.email(),
                registerRecord.phone(),
                registerRecord.role(),
                Status.ACTIVE,
                registerRecord.companyId()
        );
    }
}
