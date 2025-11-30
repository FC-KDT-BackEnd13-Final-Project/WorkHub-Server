package com.workhub.project.dto;

import java.time.LocalDate;
import java.util.List;

public record CreateProjectRequest(
        String projectName,
        String projectDescription,
        Long company,
        List<Long> managerNames,
        List<Long> developerNames,
        LocalDate starDate,
        LocalDate endDate
) {
}
