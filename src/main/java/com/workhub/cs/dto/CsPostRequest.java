package com.workhub.cs.dto;

import java.util.List;

public record CsPostRequest(
        String title,
        String content,
        List<CsPostFileRequest> files
) {
    public static CsPostRequest of(String title, String content, List<CsPostFileRequest> files) {
        return new CsPostRequest(title, content, files);
    }
}