package com.workhub.userTable.controller;

import com.workhub.file.service.UpdateProfileService;
import com.workhub.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UpdateProfileService profileService;

    @PatchMapping("/profile")
    public ResponseEntity<ApiResponse<String>> updateProfile(@RequestPart("file") MultipartFile file) {

        String profile = profileService.updateProfile(file);
        return ApiResponse.success(profile);

    }
}
