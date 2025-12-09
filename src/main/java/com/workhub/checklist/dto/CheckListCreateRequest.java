package com.workhub.checklist.dto;

import java.util.List;

public record CheckListRequest(
    String description,
    List<CheckListItemRequest> items
) {
}
