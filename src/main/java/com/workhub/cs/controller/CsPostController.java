package com.workhub.cs.controller;

import com.workhub.cs.dto.CsPostRequest;
import com.workhub.cs.dto.CsPostResponse;
import com.workhub.cs.service.CsPostService;
import com.workhub.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/projects")
public class CsPostController {

    private final CsPostService csPostService;

    @PostMapping("/{projectId}/csPosts")
    public ResponseEntity<ApiResponse<CsPostResponse>> createCsPost(
            @PathVariable Long projectId,
            @RequestBody CsPostRequest csPostRequest
    ) {

        // todo : security 기능 구현시 userId security에서 꺼내서 넘겨야 함
        CsPostResponse response = csPostService.create(projectId, csPostRequest);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.created(response, "CS 게시글이 작성되었습니다."));
    }
}
