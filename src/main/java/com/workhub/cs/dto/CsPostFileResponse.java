package com.workhub.cs.dto;

public record CsPostFileResponse(
        Long csPostFileId,
        String fileUrl,
        String fileName,
        Integer fileOrder
) {}