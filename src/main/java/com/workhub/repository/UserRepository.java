package com.workhub.repository;

import com.workhub.userTable.entity.UserTable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<UserTable, Integer> {
    Optional<UserTable> findByLoginId(String loginId);
}
