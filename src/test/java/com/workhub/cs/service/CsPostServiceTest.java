package com.workhub.cs.service;

import com.workhub.cs.entity.CsPost;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CsPostServiceTest {

    @Mock
    private CsPostRepository csPostRepository;

    @InjectMocks
    private CsPostService csPostService;

    @Test
    @DisplayName(" CS 게시글을 작성하면 작성한 게시글 정보를 보여준다.")
    void givenCsPostCreateRequest_whenCreateCsPost_thenSuccess() {
        // given
        CsPostRequest request = CsPostRequest.builder()
                .projectId(1L)
                .userId(2L)
                .title("문의 제목")
                .content("문의 내용")
                .build();

        CsPost mockSaved = CsPost.builder()
                .csPostId(1L)
                .projectId(1L)
                .userId(2L)
                .title("문의 제목")
                .content("문의 내용")
                .build();


        when(csPostRepository.save(any(CsPost.class)))
                .thenReturn(mockSaved);

        // when
        CsPost result = csPostService.create(request);

        // then
        assertThat(result.getCsPostId()).isEqualTo(1L);
        assertThat(result.getProjectId()).isEqualTo(1L);
        assertThat(result.getTitle()).isEqualTo("문의 제목");
        assertThat(result.getContent()).isEqualTo("문의 내용");

        verify(csPostRepository).save(any(CsPost.class));
    }
}
