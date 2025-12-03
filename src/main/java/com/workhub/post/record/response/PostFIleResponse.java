package com.workhub.post.record.response;

import com.workhub.post.entity.PostFile;

public record PostFIleResponse(
        Long PostFileId,
        String fileName,
        Integer fileOrder
) {
    public static PostFIleResponse from(PostFile file) {
        return new PostFIleResponse(
                file.getPostFileId(),
                file.getFileName(),
                file.getFileOrder()
        );
    }
}
