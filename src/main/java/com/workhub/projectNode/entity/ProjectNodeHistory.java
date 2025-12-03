package com.workhub.projectNode.entity;

import com.workhub.global.context.RequestContext;
import com.workhub.global.entity.ActionType;
import com.workhub.global.entity.BaseHistoryEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Getter
@SuperBuilder
@NoArgsConstructor
@Entity
@Table(name = "project_node_history")
public class ProjectNodeHistory extends BaseHistoryEntity {

    public static ProjectNodeHistory of(Long targetId, ActionType actionType, String beforeData,
                                    Long originalCreator, RequestContext context) {

        return ProjectNodeHistory.builder()
                .targetId(targetId)
                .actionType(actionType)
                .beforeData(beforeData)  // todo : 어떤 데이터가 들어가야 할 지 상의해봐야 합니다.
                .createdBy(originalCreator)
                .updatedBy(context.userId())
                .updatedAt(LocalDateTime.now())
                .ipAddress(context.userIp())
                .userAgent(context.userAgent())
                .build();
    }
}
