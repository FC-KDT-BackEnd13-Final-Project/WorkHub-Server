package com.workhub.cs.service;

import com.workhub.cs.dto.CsPostFileRequest;
import com.workhub.cs.dto.CsPostRequest;
import com.workhub.cs.dto.CsPostResponse;
import com.workhub.cs.entity.CsPost;
import com.workhub.cs.entity.CsPostFile;
import com.workhub.cs.repository.CsPostFileRepository;
import com.workhub.cs.repository.CsPostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CsPostService {

    private final CsPostRepository csPostRepository;
    private final CsPostFileRepository csPostFileRepository;

//    private final ProjectValidator projectValidator;
//    private final UserValidator userValidator;

    /**
     * CS 게시글을 작성합니다.
     * @param projectId
     * @param csPostRequest
     * @return
     */
    public CsPostResponse create(Long projectId, CsPostRequest csPostRequest) {

        // todo : userId, projectId 검증 validator 로직 필요
        // todo : project가 끝난 상태인지, 존재하는 프로젝트인지 확인 필요
        CsPost csPost = csPostRepository.save(CsPost.of(projectId, csPostRequest));

        List<CsPostFile> savedFiles = new ArrayList<>();

        if (csPostRequest.files() != null && !csPostRequest.files().isEmpty()) {
            for (CsPostFileRequest fr : csPostRequest.files()) {
                CsPostFile file = CsPostFile.of(csPost.getCsPostId(), fr);
                savedFiles.add(csPostFileRepository.save(file));
            }
        }

        return CsPostResponse.from(csPost, savedFiles);
    }
}
