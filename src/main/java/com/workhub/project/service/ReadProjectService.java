package com.workhub.project.service;

import com.workhub.global.error.ErrorCode;
import com.workhub.global.error.exception.BusinessException;
import com.workhub.global.util.SecurityUtil;
import com.workhub.project.dto.ProjectListResponse;
import com.workhub.project.entity.Project;
import com.workhub.project.entity.ProjectClientMember;
import com.workhub.project.entity.ProjectDevMember;
import com.workhub.projectNode.service.ProjectNodeService;
import com.workhub.userTable.entity.Company;
import com.workhub.userTable.entity.UserRole;
import com.workhub.userTable.entity.UserTable;
import com.workhub.userTable.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ReadProjectService {

    private final ProjectService projectService;
    private final ProjectNodeService projectNodeService;
    private final UserService userService;

    /**
     * 사용자 권한(Role)에 따라 프로젝트 목록을 조회.
     * QueryDSL 배치 조회로 N+1 문제 완전 해결 (181개 쿼리 → 6개 쿼리).
     *
     * @return 프로젝트 목록 응답 (멤버 정보, 워크플로우 단계, 총 인원 포함)
     */
    public List<ProjectListResponse> projectList() {

        List<Project> projects = getProjectsByRole();
        if (projects.isEmpty()) {
            return List.of();
        }

        // 1. 프로젝트 ID 리스트 수집
        List<Long> projectIds = projects.stream()
                .map(Project::getProjectId)
                .toList();

        // 2. 모든 프로젝트의 멤버를 한 번에 조회
        List<ProjectClientMember> allClientMembers = projectService.getClientMemberByProjectIdIn(projectIds);
        List<ProjectDevMember> allDevMembers = projectService.getDevMemberByProjectIdIn(projectIds);

        // 3. 모든 userId 수집
        Set<Long> userIds = Stream.concat(
                allClientMembers.stream().map(ProjectClientMember::getUserId),
                allDevMembers.stream().map(ProjectDevMember::getUserId)
        ).collect(Collectors.toSet());

        // 4. 모든 사용자를 한 번에 조회
        Map<Long, UserTable> userMap = userService.getUserMapByUserIdIn(List.copyOf(userIds));

        // 5. 모든 워크플로우 개수를 한 번에 조회
        Map<Long, Long> workflowCountMap = projectNodeService.getProjectNodeCountMapByProjectIdIn(projectIds);

        // 6. projectId별로 멤버 그룹핑
        Map<Long, List<ProjectClientMember>> clientMemberMap = allClientMembers.stream()
                .collect(Collectors.groupingBy(ProjectClientMember::getProjectId));
        Map<Long, List<ProjectDevMember>> devMemberMap = allDevMembers.stream()
                .collect(Collectors.groupingBy(ProjectDevMember::getProjectId));

        // 7. Response 생성
        return projects.stream()
                .map(project -> buildProjectResponse(
                        project,
                        clientMemberMap.getOrDefault(project.getProjectId(), List.of()),
                        devMemberMap.getOrDefault(project.getProjectId(), List.of()),
                        userMap,
                        workflowCountMap.getOrDefault(project.getProjectId(), 0L)
                ))
                .toList();
    }

    /**
     * 사용자 Role에 따라 접근 가능한 프로젝트 목록을 조회.
     * CLIENT/DEVELOPER는 자신이 속한 프로젝트만, ADMIN은 전체 조회.
     * 배치 조회로 N+1 문제 해결.
     *
     * @return 권한에 따른 프로젝트 목록
     */
    private List<Project> getProjectsByRole() {

        Long userId = SecurityUtil.getCurrentUserId()
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_LOGGED_IN));
        UserTable user = userService.getUserById(userId);
        UserRole role = user.getRole();

        return switch (role) {
            case CLIENT -> {
                List<ProjectClientMember> clientMembers = projectService.getClientMemberByUserId(userId);
                List<Long> projectIds = clientMembers.stream()
                        .map(ProjectClientMember::getProjectId)
                        .toList();
                yield getProjectsByIds(projectIds);
            }
            case DEVELOPER -> {
                List<ProjectDevMember> devMembers = projectService.getDevMemberByUserId(userId);
                List<Long> projectIds = devMembers.stream()
                        .map(ProjectDevMember::getProjectId)
                        .toList();
                yield getProjectsByIds(projectIds);
            }
            case ADMIN -> projectService.findAll();
            default -> throw new BusinessException(ErrorCode.INVALID_USER_ROLE);
        };
    }

    /**
     * 프로젝트 ID 리스트로 프로젝트 목록을 배치 조회.
     * 빈 리스트인 경우 불필요한 쿼리 방지.
     *
     * @param projectIds 프로젝트 ID 리스트
     * @return 프로젝트 목록
     */
    private List<Project> getProjectsByIds(List<Long> projectIds) {
        if (projectIds.isEmpty()) {
            return List.of();
        }
        return projectService.findByProjectIdIn(projectIds);
    }

    /**
     * 배치 조회된 데이터를 활용하여 프로젝트 응답 객체 생성.
     * 추가 DB 쿼리 없이 메모리에서 조합.
     *
     * @param project 프로젝트 엔티티
     * @param clientMembers 클라이언트 멤버 목록
     * @param devMembers 개발자 멤버 목록
     * @param userMap 사용자 정보 맵 (배치 조회 결과)
     * @param workflowCount 워크플로우 단계 개수
     * @return 프로젝트 응답 객체
     */
    private ProjectListResponse buildProjectResponse(
            Project project,
            List<ProjectClientMember> clientMembers,
            List<ProjectDevMember> devMembers,
            Map<Long, UserTable> userMap,
            Long workflowCount
    ) {
        List<UserTable> clientList = clientMembers.stream()
                .map(ProjectClientMember::getUserId)
                .map(userId -> {
                    UserTable user = userMap.get(userId);
                    if (user == null) {
                        log.warn("Client user not found in batch result. ProjectId={}, UserId={}",
                                project.getProjectId(), userId);
                    }
                    return user;
                })
                .filter(Objects::nonNull)
                .toList();

        List<UserTable> devList = devMembers.stream()
                .map(ProjectDevMember::getUserId)
                .map(userId -> {
                    UserTable user = userMap.get(userId);
                    if (user == null) {
                        log.warn("Developer user not found in batch result. ProjectId={}, UserId={}",
                                project.getProjectId(), userId);
                    }
                    return user;
                })
                .filter(Objects::nonNull)
                .toList();

        Company company = getTempCompany();

        return ProjectListResponse.from(project, clientList, devList, workflowCount, company);
    }

    /**
     * 임시 고객사 정보 생성.
     * TODO: 고객사 기능 완성 시 실제 데이터로 교체.
     *
     * @return 더미 고객사 객체
     */
    private Company getTempCompany() {
        // todo : 고객사 기능 완성 시 실제 데이터로 교체
        return Company.builder()
                .companyId(1L)
                .companyName("멍뭉이")
                .companyNumber("1231212345")
                .tel("1212312312")
                .address("서울시 강남구")
                .build();
    }
}
