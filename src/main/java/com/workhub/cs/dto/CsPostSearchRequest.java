package com.workhub.cs.dto;

import com.workhub.cs.entity.CsPostStatus;

public record CsPostSearchType(
        String searchValue,
        CsPostStatus csPostStatus
) {
}
