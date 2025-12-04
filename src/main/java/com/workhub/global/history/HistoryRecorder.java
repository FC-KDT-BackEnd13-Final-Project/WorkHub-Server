package com.workhub.global.history;

import com.workhub.global.entity.ActionType;
import com.workhub.global.entity.BaseHistoryEntity;
import com.workhub.global.entity.HistoryType;
import com.workhub.global.repository.BaseHistoryRepository;
import com.workhub.global.util.SecurityUtil;
import com.workhub.project.entity.ProjectHistory;
import com.workhub.project.repository.ProjectHistoryRepository;
import com.workhub.projectNode.entity.ProjectNodeHistory;
import com.workhub.projectNode.repository.ProjectNodeHistoryRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class HistoryRecorder {

    private final ProjectHistoryRepository projectHistoryRepository;
    private final ProjectNodeHistoryRepository projectNodeHistoryRepository;
    // 새로은 히스토리 리포지토리 추가 시 여기에 주입

    private Map<HistoryType, HistoryHandler> handlerMap;

    @PostConstruct
    public void init() {
        handlerMap = new HashMap<>();

        // PROJECT 핸들러 등록
        handlerMap.put(HistoryType.PROJECT, new HistoryHandler(
                projectHistoryRepository,
                ProjectHistory::of  // 메서드 레퍼런스
        ));

        // PROJECT_NODE 핸들러 등록
        handlerMap.put(HistoryType.PROJECT_NODE, new HistoryHandler(
                projectNodeHistoryRepository,
                ProjectNodeHistory::of
        ));

        // 새로운 타입 추가 시 여기에 등록만 하면 됨.
        // handlerMap.put(HistoryType.XXX, new HistoryHandler(xxxRepository, XXXHistory::of));
    }

    /**
     * 통합 히스토리 저장 메서드
     */
    public void recordHistory(HistoryType type,
                              Long targetId,
                              ActionType actionType,
                              String beforeData) {

        Long creator;

        if (actionType == ActionType.CREATE) {
            // CREATE 액션일 때는 현재 사용자가 생성자
            creator = SecurityUtil.getCurrentUserIdOrThrow();
        } else {
            // UPDATE, DELETE 등일 때는 원래 생성자를 조회
            creator = getOriginalCreator(type, targetId);
        }

        HistoryHandler handler = getHandler(type);

        BaseHistoryEntity history = handler.createHistory(
                targetId, actionType, beforeData, creator
        );
        handler.save(history);

        log.debug("History recorded: type={}, targetId={}, action={}",
                type, targetId, actionType);
    }

    /**
     * originalCreator 조회
     */
    public Long getOriginalCreator(HistoryType type, Long targetId) {
        HistoryHandler handler = getHandler(type);
        return handler.findOriginalCreator(targetId);
    }

    private HistoryHandler getHandler(HistoryType type) {
        HistoryHandler handler = handlerMap.get(type);
        if (handler == null) {
            throw new IllegalArgumentException("No handler registered for type: " + type);
        }
        return handler;
    }

    /**
     * 히스토리 생성 함수형 인터페이스
     */
    @FunctionalInterface
    public interface HistoryCreator {
        BaseHistoryEntity create(Long targetId, ActionType actionType, String beforeData,
                                 Long creator);
    }

    /**
     * 히스토리 핸들러 (생성 + 저장 + 조회)
     */
    @RequiredArgsConstructor
    private class HistoryHandler {
        private final JpaRepository<? extends BaseHistoryEntity, Long> repository;
        private final HistoryCreator creator;

        public BaseHistoryEntity createHistory(Long targetId, ActionType actionType,
                                               String beforeData, Long creator) {
            return this.creator.create(targetId, actionType, beforeData, creator);
        }

        @SuppressWarnings("unchecked")
        public void save(BaseHistoryEntity history) {
            ((JpaRepository<BaseHistoryEntity, Long>) repository).save(history);
        }

        public Long findOriginalCreator(Long targetId) {
            // BaseHistoryRepository 인터페이스 필요 (아래 참고)
            if (repository instanceof BaseHistoryRepository) {
                return ((BaseHistoryRepository<?>) repository)
                        .findFirstByTargetIdAndActionTypeOrderByChangeLogIdAsc(
                                targetId, ActionType.CREATE
                        )
                        .map(BaseHistoryEntity::getCreatedBy)
                        .orElse(null);
            }
            throw new IllegalStateException("Repository must extend BaseHistoryRepository");
        }
    }
}