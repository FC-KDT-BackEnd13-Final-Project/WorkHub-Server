package com.workhub.global.aspect;

import com.workhub.global.context.RequestContext;
import com.workhub.global.entity.ActionType;
import com.workhub.global.entity.HistoryType;
import com.workhub.global.error.ErrorCode;
import com.workhub.global.error.exception.BusinessException;
import com.workhub.project.entity.Project;
import com.workhub.project.entity.ProjectHistory;
import com.workhub.project.repository.ProjectHistoryRepository;
import com.workhub.projectNode.entity.ProjectNode;
import com.workhub.projectNode.entity.ProjectNodeHistory;
import com.workhub.projectNode.repository.ProjectNodeHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class HistoryRecordAspect {

    private final ProjectHistoryRepository projectHistoryRepository;
    private final ProjectNodeHistoryRepository projectNodeHistoryRepository;

    @Around("@annotation(recordHistory)")
    public Object recordHistory(ProceedingJoinPoint joinPoint, RecordHistory recordHistory) throws Throwable {

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Object[] args = joinPoint.getArgs();

        // 공통 정보 추출
        Long userId = extractUserId(args, signature);
        String userIp = extractUserIp(args, signature);
        String userAgent = extractUserAgent(args, signature);

        String beforeData = null;
        Long targetId = null;
        Long originalCreator = null;

        // UPDATE인 경우: 파라미터에서 beforeData와 targetId 추출
        if (recordHistory.action() == ActionType.UPDATE) {
            targetId = extractTargetIdFromArgs(args, signature);
            beforeData = extractBeforeDataFromArgs(args, signature);
            originalCreator = getOriginalCreator(recordHistory.type(), targetId);
        }

        // 메서드 실행
        Object result = joinPoint.proceed();

        // CREATE인 경우: 메서드 실행 후 targetId와 beforeData 추출
        if (recordHistory.action() == ActionType.CREATE) {
            targetId = extractTargetIdFromResult(result, recordHistory.type());
            beforeData = extractBeforeDataFromResult(result, recordHistory.type());
        }

        // 히스토리 저장
        saveHistory(recordHistory.type(), targetId, recordHistory.action(),
                beforeData, originalCreator, userId, userIp, userAgent);

        return result;
    }

    // UPDATE 시 파라미터에서 targetId 추출
    private Long extractTargetIdFromArgs(Object[] args, MethodSignature signature) {
        String[] paramNames = signature.getParameterNames();
        for (int i = 0; i < paramNames.length; i++) {
            if (paramNames[i].equals("projectId") || paramNames[i].equals("nodeId")) {
                return (Long) args[i];
            }
        }
        throw new IllegalStateException("targetId not found in method parameters");
    }

    // 파라미터에서 beforeData 추출 (UPDATE 시 사용)
    private String extractBeforeDataFromArgs(Object[] args, MethodSignature signature) {
        String[] paramNames = signature.getParameterNames();
        for (int i = 0; i < paramNames.length; i++) {
            if (paramNames[i].equals("beforeData") || paramNames[i].equals("beforeStatus")) {
                return args[i] != null ? args[i].toString() : null;
            }
        }
        throw new IllegalStateException("beforeData or beforeStatus not found in method parameters");
    }

    // CREATE 시 리턴 값에서 targetId 추출
    private Long extractTargetIdFromResult(Object result, HistoryType type) {
        return switch (type) {
            case PROJECT -> ((Project) result).getProjectId();
            case PROJECT_NODE -> ((ProjectNode) result).getProjectNodeId();
        };
    }

    // CREATE 시 리턴 값에서 beforeData 추출
    private String extractBeforeDataFromResult(Object result, HistoryType type) {
        return switch (type) {
            case PROJECT -> ((Project) result).getProjectDescription();
            case PROJECT_NODE -> ((ProjectNode) result).getDescription();
        };
    }

    private Long extractUserId(Object[] args, MethodSignature signature) {

        String[] paramNames = signature.getParameterNames();

        for (int i = 0; i < paramNames.length; i++) {
            if (paramNames[i].equals("loginUser") || paramNames[i].equals("userId")) {
                return (Long) args[i];
            }
        }

        throw new IllegalStateException("userId or loginUser not found");
    }

    private String extractUserIp(Object[] args, MethodSignature signature) {

        String[] paramNames = signature.getParameterNames();

        for (int i = 0; i < paramNames.length; i++) {
            if (paramNames[i].equals("userIp")) {
                return (String) args[i];
            }
        }

        throw new IllegalStateException("userIp not found in method parameters");
    }

    private String extractUserAgent(Object[] args, MethodSignature signature) {

        String[] paramNames = signature.getParameterNames();

        for (int i = 0; i < paramNames.length; i++) {
            if (paramNames[i].equals("userAgent")) {
                return (String) args[i];
            }
        }

        throw new IllegalStateException("userAgent not found in method parameters");
    }

    private Long getOriginalCreator(HistoryType type, Long targetId) {

        return switch (type) {
            case PROJECT -> projectHistoryRepository
                    .findFirstByTargetIdAndActionTypeOrderByChangeLogIdAsc(targetId, ActionType.CREATE)
                    .map(ProjectHistory::getCreatedBy)
                    .orElseThrow(() -> new BusinessException(ErrorCode.PROJECT_HISTORY_NOT_FOUND));
            case PROJECT_NODE -> projectNodeHistoryRepository
                    .findFirstByTargetIdAndActionTypeOrderByChangeLogIdAsc(targetId, ActionType.CREATE)
                    .map(ProjectNodeHistory::getCreatedBy)
                    .orElseThrow(() -> new BusinessException(ErrorCode.PROJECT_NODE_NOT_FOUND));
        };
    }

    private void saveHistory(HistoryType type, Long targetId, ActionType actionType,
                             String beforeData, Long originalCreator, Long userId,
                             String userIp, String userAgent) {

        Long creator = originalCreator != null ? originalCreator : userId;
        RequestContext context = RequestContext.of(userId, userIp, userAgent);

        switch (type) {
            case PROJECT -> projectHistoryRepository.save(
                    ProjectHistory.of(targetId, actionType, beforeData, creator, context)
            );
            case PROJECT_NODE -> projectNodeHistoryRepository.save(
                    ProjectNodeHistory.of(targetId, actionType, beforeData, creator, context)
            );
        }
    }
}
