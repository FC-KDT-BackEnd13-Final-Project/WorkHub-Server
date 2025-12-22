package com.workhub.dashboard.service;

import com.workhub.dashboard.dto.DashBoardResponse;
import com.workhub.project.entity.ProjectClientMember;
import com.workhub.project.entity.ProjectDevMember;
import com.workhub.project.repository.ClientMemberRepository;
import com.workhub.project.repository.DevMemberRepository;
import com.workhub.projectNode.entity.NodeStatus;
import com.workhub.projectNode.repository.ProjectNodeRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class DashBoardServiceTest {

    @Mock
    DevMemberRepository devMemberRepository;
    @Mock
    ClientMemberRepository clientMemberRepository;
    @Mock
    ProjectNodeRepository projectNodeRepository;

    @InjectMocks
    DashBoardService dashBoardService;

    @Test
    @DisplayName("개발사 역할이면 자신의 프로젝트에서 PENDING 노드 수를 반환한다")
    void getSummary_asDev_countsPending() {
        given(devMemberRepository.findByUserId(1L)).willReturn(List.of(
                ProjectDevMember.builder().projectId(10L).build(),
                ProjectDevMember.builder().projectId(11L).build()
        ));
        given(projectNodeRepository.countByProjectIdInAndNodeStatusIn(anyList(), eq(List.of(NodeStatus.PENDING_REVIEW))))
                .willReturn(5L);

        DashBoardResponse res = dashBoardService.getSummary(1L, "DEV");

        assertThat(res.pendingApprovals()).isEqualTo(5L);
        assertThat(res.totalProjects()).isEqualTo(2L);
    }

    @Test
    @DisplayName("고객사 역할이면 자신의 프로젝트에서 PENDING 노드 수를 반환한다")
    void getSummary_asClient_countsPending() {
        given(clientMemberRepository.findByUserId(2L)).willReturn(List.of(
                ProjectClientMember.builder().projectId(20L).build(),
                ProjectClientMember.builder().projectId(21L).build()
        ));
        given(projectNodeRepository.countByProjectIdInAndNodeStatusIn(anyList(), eq(List.of(NodeStatus.PENDING_REVIEW))))
                .willReturn(3L);

        DashBoardResponse res = dashBoardService.getSummary(2L, "CLIENT");

        assertThat(res.pendingApprovals()).isEqualTo(3L);
        assertThat(res.totalProjects()).isEqualTo(2L);
    }

    @Test
    @DisplayName("소속 프로젝트가 없으면 0,0을 반환한다")
    void getSummary_emptyProjects_returnsZero() {
        given(devMemberRepository.findByUserId(3L)).willReturn(List.of());

        DashBoardResponse res = dashBoardService.getSummary(3L, "DEV");

        assertThat(res.pendingApprovals()).isZero();
        assertThat(res.totalProjects()).isZero();
    }
}
