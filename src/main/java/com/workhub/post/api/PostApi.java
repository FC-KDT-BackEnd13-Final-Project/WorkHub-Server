package com.workhub.post.service;

import com.workhub.global.error.ErrorCode;
import com.workhub.global.error.exception.BusinessException;
import com.workhub.post.entity.HashTag;
import com.workhub.post.entity.Post;
import com.workhub.post.entity.PostType;
import com.workhub.post.record.request.PostRequest;
import com.workhub.post.record.request.PostUpdateRequest;
import com.workhub.post.repository.PostRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.ArgumentCaptor;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.Optional;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(SpringExtension.class)
public class PostServiceTest {
    @Mock
    PostRepository postRepository;
    @InjectMocks
    PostService postService;

    @Test
    @DisplayName("부모 게시물이 없으면 예외를 던진다.")
    void create_withParentNotFound_shouldThrow() {
        PostRequest request = new PostRequest(
                "title", PostType.NOTICE, "content", "11.1.1",1L, HashTag.DESIGN
        );
        given(postRepository.existsByPostIdAndDeletedAtIsNull(1L)).willReturn(false);

        assertThatThrownBy(() -> postService.create(request))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PARENT_POST_NOT_FOUND);
    }

    @Test
    @DisplayName("부모 게시글이 이미 삭제된 경우 예외를 던진다")
    void create_withDeletedParent_shouldThrowAlreadyDeleted() {
        PostRequest request = new PostRequest(
                "title", PostType.NOTICE, "content", "11.1.1",1L, HashTag.DESIGN
        );
        given(postRepository.existsByPostIdAndDeletedAtIsNull(1L)).willReturn(false);

        assertThatThrownBy(() -> postService.create(request))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PARENT_POST_NOT_FOUND);
    }

    @Test
    @DisplayName("정상적으로 게시글을 생성하면 저장된 엔티티를 반환한다")
    void create_success_shouldReturnSavedPost() {
        Post saved = Post.builder()
                .postId(10L)
                .title("title")
                .content("content")
                .type(PostType.NOTICE)
                .postIp("127.0.0.1")
                .hashtag(HashTag.DESIGN)
                .build();
        given(postRepository.save(any(Post.class))).willReturn(saved);

        PostRequest request = new PostRequest(
                "title", PostType.NOTICE, "content", "127.0.0.1", null, HashTag.DESIGN
        );

        Post result = postService.create(request);

        assertThat(result.getPostId()).isEqualTo(10L);
        assertThat(result.getTitle()).isEqualTo("title");
    }

    @Test
    @DisplayName("게시글을 수정하면 필드가 갱신된다")
    void update_success_shouldChangeFields() {
        Post origin = Post.builder()
                .postId(1L)
                .title("old")
                .content("old")
                .type(PostType.GENERAL)
                .postIp("1.1.1.1")
                .hashtag(HashTag.REQ_DEF)
                .build();

        PostUpdateRequest request = new PostUpdateRequest(
                "new", PostType.NOTICE, "new content", "2.2.2.2", HashTag.DESIGN
        );

        Post result = postService.update(origin, request);

        assertThat(result.getTitle()).isEqualTo("new");
        assertThat(result.getPostIp()).isEqualTo("2.2.2.2");
    }

    @Test
    @DisplayName("게시글 조회 시 존재하지 않으면 예외를 던진다")
    void findById_withPostNotFound_shouldThrow() {
        given(postRepository.findByPostIdAndDeletedAtIsNull(99L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> postService.findById(99L))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.POST_NOT_FOUND);
    }

    @Test
    @DisplayName("삭제 대상 게시글이 없으면 예외를 던진다")
    void delete_withPostNotFound_shouldThrow() {
        given(postRepository.findByPostIdAndDeletedAtIsNull(99L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> postService.delete(99L))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.POST_NOT_FOUND);
    }

    @Test
    @DisplayName("이미 삭제된 게시글을 삭제하려 하면 예외를 던진다")
    void delete_withAlreadyDeletedPost_shouldThrow() {
        given(postRepository.findByPostIdAndDeletedAtIsNull(1L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> postService.delete(1L))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.POST_NOT_FOUND);
    }

    @Test
    @DisplayName("게시글 삭제 성공 시 repository.delete가 호출된다")
    void delete_success_shouldInvokeDelete() {
        Post existing = Post.builder()
                .postId(1L)
                .title("title")
                .content("content")
                .type(PostType.NOTICE)
                .build();
        given(postRepository.findByPostIdAndDeletedAtIsNull(1L)).willReturn(Optional.of(existing));

        postService.delete(1L);

        assertThat(existing.isDeleted()).isTrue();
    }

    @Test
    @DisplayName("검색 조건과 pageable 정보를 그대로 Repository에 전달한다")
    void search_shouldDelegateToRepositoryWithSpecificationAndPageable() {
        PageRequest pageable = PageRequest.of(1, 5);
        Post post = Post.builder()
                .postId(1L)
                .title("title")
                .content("content")
                .type(PostType.NOTICE)
                .hashtag(HashTag.DESIGN)
                .build();
        Page<Post> page = new PageImpl<>(List.of(post), pageable, 1);
        given(postRepository.findAll(any(Specification.class), any(Pageable.class))).willReturn(page);


        Page<Post> result = postService.search(1L, 2L, "title", PostType.NOTICE, HashTag.DESIGN, pageable);

        assertThat(result.getContent()).isNotNull();

        ArgumentCaptor<Specification<Post>> specCaptor = ArgumentCaptor.forClass(Specification.class);
        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(postRepository).findAll(specCaptor.capture(), pageableCaptor.capture());
        assertThat(specCaptor.getValue()).isNotNull();
        assertThat(pageableCaptor.getValue()).isEqualTo(pageable);
    }



}
