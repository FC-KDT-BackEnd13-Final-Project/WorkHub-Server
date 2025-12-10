package com.workhub.userTable.dto;

import com.workhub.userTable.entity.Status;
import jakarta.validation.constraints.NotNull;

public record CompanyStatusUpdateRequest(
        @NotNull
        Status status
) {
}
