package com.workhub.cs.dto;

public record CsPostFileRequest(
        String fileUrl,
        String fileName,
        Integer fileOrder
) {
}