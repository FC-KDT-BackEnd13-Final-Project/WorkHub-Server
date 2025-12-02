package com.workhub.cs.dto.csQna;

import com.workhub.cs.entity.CsQna;

import java.time.LocalDateTime;

public record CsQnsResponse(
        Long csQnaId,
        Long csPostId,
        Long userId,
        Long parentQnaId,
        String qnaContent,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static CsQnsResponse from(CsQna csQna) {
        return new CsQnsResponse(
                csQna.getCsQnaId(),
                csQna.getCsPostId(),
                csQna.getUserId(),
                csQna.getParentQnaId(),
                csQna.getQnaContent(),
                csQna.getCreatedAt(),
                csQna.getUpdatedAt()
        );
    }
}
