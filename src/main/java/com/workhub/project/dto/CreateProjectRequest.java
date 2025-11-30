package com.workhub.project.dto;

import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;
import java.util.List;

public record CreateProjectRequest(

        @NotBlank(message = "프로젝트명은 빈값일 수 없습니다.")
        String projectName,
        @NotBlank(message = "프로젝트 설명은 빈값일 수 없습니다.")
        String projectDescription,
        @NotBlank
        Long company,
        @NotBlank
        List<Long> managerNames,
        @NotBlank
        List<Long> developerNames,
        @NotBlank
        LocalDate starDate,
        @NotBlank
        LocalDate endDate
) {
}
