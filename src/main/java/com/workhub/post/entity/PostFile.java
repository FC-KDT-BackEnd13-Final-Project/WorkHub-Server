package com.workhub.post.entity;

import com.workhub.global.entity.BaseTimeEntity;
import com.workhub.post.record.request.PostFileRequest;
import com.workhub.post.repository.PostFileRepository;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "post_file")
@Getter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class PostFile extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "post_file_id")
    private Long postFileId;

    @Column(name = "file_url", length = 255)
    private String fileUrl;

    @Column(name = "file_name", length = 255)
    private String fileName;

    @Column(name = "post_id")
    private Long postId;

    @Column(name = "file_order")
    private Integer fileOrder;

    public static PostFile of(Long postId, PostFileRequest request) {
        return PostFile.builder()
                .postFileId(postId)
                .fileName(request.fileName())
                .fileOrder(request.fileOrder())
                .build();
    }

}
