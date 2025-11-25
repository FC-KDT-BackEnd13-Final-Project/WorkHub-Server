package com.workhub.cs.service;

import com.workhub.cs.dto.CsPostRequest;
import com.workhub.cs.dto.CsPostResponse;
import com.workhub.cs.entity.CsPost;
import com.workhub.cs.repository.CsPostRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CsPostServiceTest {

    @Mock
    private CsPostRepository csPostRepository;

    @InjectMocks
    private CsPostService csPostService;

    private CsPost mockSaved;

    @BeforeEach
    public void init(){
        mockSaved = CsPost.builder()
                .csPostId(1L)
                .projectId(1L)
                .userId(2L)
                .title("문의 제목")
                .content("문의 내용")
                .build();
    }

    @Test
    @DisplayName(" CS 게시글을 작성하면 작성한 게시글 정보를 보여준다.")
    void givenCsPostCreateRequest_whenCreateCsPost_thenSuccess() {
        // given
        Long projectId = 1L;
        CsPostRequest request = CsPostRequest.of("문의 제목", "문의 내용");

        when(csPostRepository.save(any(CsPost.class)))
                .thenReturn(mockSaved);

        // when
        CsPostResponse result = csPostService.create(projectId, request);

        // then
        assertThat(result.getCsPostId()).isEqualTo(1L);
        assertThat(result.getTitle()).isEqualTo("문의 제목");
        assertThat(result.getContent()).isEqualTo("문의 내용");

        verify(csPostRepository).save(any(CsPost.class));
    }

    // todo : 프로젝트 밸리데이터 붙이고 나서 테스트 해야 함
    @Test
    @DisplayName("프로젝트가 존재하지 않으면 ProjectNotFoundException이 발생한다.")
    void givenInvalidProject_whenCreate_thenThrowProjectNotFound() {

    }

    @Test
    @DisplayName("요청 DTO가 매핑되어 엔티티로 저장되는지 검증한다.")
    void givenRequest_whenCreate_thenEntityMappedSuccessfully() {
        // given
        Long projectId = 1L;
        Long userId = 2L;

        CsPostRequest request = CsPostRequest.of("문의 제목", "문의 내용");
        when(csPostRepository.save(any(CsPost.class)))
                .thenReturn(mockSaved);

        // when
        csPostService.create(projectId, request);

        // then
        verify(csPostRepository).save(argThat(post ->
                post.getProjectId().equals(projectId) &&
                        // post.getUserId().equals(userId) && // todo : security 적용 전이라 주석
                        post.getTitle().equals("문의 제목") &&
                        post.getContent().equals("문의 내용")
        ));
    }
}
