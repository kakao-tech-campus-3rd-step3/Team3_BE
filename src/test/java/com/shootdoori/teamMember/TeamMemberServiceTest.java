package com.shootdoori.teamMember;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.shootdoori.match.dto.TeamMemberMapper;
import com.shootdoori.match.dto.TeamMemberRequestDto;
import com.shootdoori.match.dto.TeamMemberResponseDto;
import com.shootdoori.match.dto.UpdateTeamMemberRequestDto;
import com.shootdoori.match.entity.SkillLevel;
import com.shootdoori.match.entity.Team;
import com.shootdoori.match.entity.TeamMember;
import com.shootdoori.match.entity.TeamMemberRole;
import com.shootdoori.match.entity.TeamType;
import com.shootdoori.match.entity.User;
import com.shootdoori.match.exception.common.DifferentException;
import com.shootdoori.match.exception.common.DuplicatedException;
import com.shootdoori.match.exception.common.NoPermissionException;
import com.shootdoori.match.exception.common.NotFoundException;
import com.shootdoori.match.repository.ProfileRepository;
import com.shootdoori.match.repository.TeamMemberRepository;
import com.shootdoori.match.repository.TeamRepository;
import com.shootdoori.match.service.TeamMemberService;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
@DisplayName("TeamMemberService 테스트")
public class TeamMemberServiceTest {

    private static final Long TEAM_ID = 1L;
    private static final Long USER_ID = 1L;
    private static final Long ANOTHER_USER_ID = 2L;
    private static final Long NON_EXISTENT_TEAM_ID = 100L;
    private static final Long NON_EXISTENT_USER_ID = 100L;
    private static final int PAGE = 0;
    private static final int SIZE = 10;
    private static final String ROLE_MEMBER = "일반멤버";
    private static final String ROLE_LEADER = "회장";
    private static final String ROLE_VICE_LEADER = "부회장";

    @Mock
    private TeamMemberRepository teamMemberRepository;

    @Mock
    private TeamRepository teamRepository;

    @Mock
    private ProfileRepository profileRepository;

    @Mock
    private TeamMemberMapper teamMemberMapper;

    private TeamMemberService teamMemberService;

    private Team team;
    private User captain;
    private User user;
    private User anotherUser;
    private TeamMember teamMember;

    @BeforeEach
    void setUp() {
        teamMemberService = new TeamMemberService(teamMemberRepository, teamRepository,
            profileRepository, teamMemberMapper);

        captain = User.create(
            "김학생",
            "아마추어",
            "student@example.com",
            "student@kangwon.ac.kr",
            "Abcd1234!",
            "010-1234-5678",
            "골키퍼",
            "강원대학교",
            "컴퓨터공학과",
            "25",
            "축구를 좋아하는 대학생입니다. 골키퍼 포지션을 주로 맡고 있으며, 즐겁게 운동하고 싶습니다!"
        );

        user = User.create(
            "손응민",
            "세미프로",
            "student999@gmail.com",
            "student35@kangwon.ac.kr",
            "Abcd1234!",
            "010-0000-0000",
            "풀백",
            "강원대학교",
            "컴퓨터공학과",
            "35",
            "축구 좋아하는 아빠입니다."
        );

        team = new Team(
            "강원대 FC",
            captain,
            "강원대학교",
            TeamType.CENTRAL_CLUB,
            SkillLevel.AMATEUR,
            "주 2회 연습합니다."
        );

        team.recruitMember(captain, TeamMemberRole.LEADER);

        teamMember = new TeamMember(team, captain, TeamMemberRole.LEADER);

        anotherUser = User.create(
            "손응민",
            "세미프로",
            "student999@gmail.com",
            "student35@kangwon.ac.kr",
            "Abcd1234!",
            "010-0000-0000",
            "풀백",
            "강원대학교",
            "컴퓨터공학과",
            "35",
            "축구 좋아하는 아빠입니다."
        );

        ReflectionTestUtils.setField(anotherUser, "id", ANOTHER_USER_ID);
    }

    @Nested
    @DisplayName("create")
    class CreateTest {

        @Test
        @DisplayName("create - 정상 생성")
        void create_success() {
            // given
            TeamMemberRequestDto requestDto = new TeamMemberRequestDto(
                ANOTHER_USER_ID,
                ROLE_MEMBER
            );
            ReflectionTestUtils.setField(captain, "id", 1L);

            TeamMember anotherTeamMember = new TeamMember(team, anotherUser, TeamMemberRole.MEMBER);

            TeamMemberResponseDto expected = toResponse(anotherTeamMember);

            when(teamRepository.findById(TEAM_ID)).thenReturn(Optional.of(team));
            when(profileRepository.findById(ANOTHER_USER_ID)).thenReturn(Optional.of(anotherUser));
            when(teamMemberRepository.existsByTeam_TeamIdAndUser_Id(TEAM_ID,
                ANOTHER_USER_ID)).thenReturn(false);
            when(teamRepository.save(any(Team.class))).thenReturn(team);
            when(teamMemberRepository.findByTeam_TeamIdAndUser_Id(TEAM_ID,
                ANOTHER_USER_ID)).thenReturn(Optional.of(anotherTeamMember));
            when(teamMemberMapper.toTeamMemberResponseDto(anotherTeamMember)).thenReturn(expected);

            // when
            TeamMemberResponseDto resultDto = teamMemberService.create(TEAM_ID, requestDto,
                captain.getId());

            // then
            assertThat(resultDto).isEqualTo(expected);
        }

        @Test
        @DisplayName("create - 팀 없음 예외")
        void create_teamNotFound_throws() {
            // given
            TeamMemberRequestDto requestDto = new TeamMemberRequestDto(
                USER_ID,
                ROLE_MEMBER
            );
            ReflectionTestUtils.setField(captain, "id", 1L);

            when(teamRepository.findById(TEAM_ID)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> teamMemberService.create(TEAM_ID, requestDto, captain.getId()))
                .isInstanceOf(NotFoundException.class);
        }

        @Test
        @DisplayName("create - 유저 없음 예외")
        void create_userNotFound_throws() {
            // given
            TeamMemberRequestDto requestDto = new TeamMemberRequestDto(
                USER_ID,
                ROLE_MEMBER
            );
            ReflectionTestUtils.setField(captain, "id", 1L);

            when(teamRepository.findById(TEAM_ID)).thenReturn(Optional.of(team));
            when(profileRepository.findById(USER_ID)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> teamMemberService.create(TEAM_ID, requestDto, captain.getId()))
                .isInstanceOf(NotFoundException.class);
        }

        @Test
        @DisplayName("create - 이미 팀 멤버 예외")
        void create_alreadyMember_throws() {
            // given
            TeamMemberRequestDto requestDto = new TeamMemberRequestDto(
                USER_ID,
                ROLE_MEMBER
            );
            ReflectionTestUtils.setField(captain, "id", 1L);

            when(teamRepository.findById(TEAM_ID)).thenReturn(Optional.of(team));
            when(profileRepository.findById(USER_ID)).thenReturn(Optional.of(user));
            when(teamMemberRepository.existsByTeam_TeamIdAndUser_Id(TEAM_ID, USER_ID)).thenReturn(
                true);

            // when & then
            assertThatThrownBy(() -> teamMemberService.create(TEAM_ID, requestDto, captain.getId()))
                .isInstanceOf(DuplicatedException.class);
        }
    }

    @Nested
    @DisplayName("findByTeamIdAndUserId")
    class FindByTeamIdAndUserIdTest {

        @Test
        @DisplayName("findByTeamIdAndUserId - 성공")
        void find_success() {
            // given
            TeamMemberResponseDto expected = toResponse(teamMember);

            when(teamMemberRepository.findByTeam_TeamIdAndUser_Id(TEAM_ID, USER_ID)).thenReturn(
                Optional.of(teamMember));

            when(teamMemberMapper.toTeamMemberResponseDto(teamMember)).thenReturn(expected);

            // when
            TeamMemberResponseDto resultDto = teamMemberService.findByTeamIdAndUserId(TEAM_ID,
                USER_ID);

            // then
            assertThat(resultDto).isEqualTo(expected);
        }

        @Test
        @DisplayName("findByTeamIdAndUserId - 미존재 예외")
        void find_notFound_throws() {
            // given
            when(teamMemberRepository.findByTeam_TeamIdAndUser_Id(TEAM_ID, USER_ID)).thenReturn(
                Optional.empty());

            // when & then
            assertThatThrownBy(() ->
                teamMemberService.findByTeamIdAndUserId(TEAM_ID, USER_ID))
                .isInstanceOf(NotFoundException.class);
        }
    }

    @Nested
    @DisplayName("findAllByTeamId")
    class FindAllByTeamIdTest {

        @Test
        @DisplayName("findAllByTeamId - 페이징/정렬 및 매핑")
        void findAll_success() {
            // given
            List<TeamMember> teamMembers = List.of(teamMember);
            Pageable pageable = PageRequest.of(PAGE, SIZE, Sort.by("id").ascending());
            Page<TeamMember> teamMemberPage = new PageImpl<>(teamMembers, pageable,
                teamMembers.size());

            TeamMemberResponseDto expected = toResponse(teamMember);

            when(teamMemberRepository.findAllByTeam_TeamId(TEAM_ID, pageable)).thenReturn(
                teamMemberPage);
            when(teamMemberMapper.toTeamMemberResponseDto(teamMember)).thenReturn(expected);

            // when
            Page<TeamMemberResponseDto> responseDtos = teamMemberService.findAllByTeamId(TEAM_ID,
                PAGE,
                SIZE);

            // then
            assertThat(responseDtos).isNotNull();
            assertThat(responseDtos.getContent()).hasSize(1);
            assertThat(responseDtos.getTotalElements()).isEqualTo(1);
            assertThat(responseDtos.getNumber()).isEqualTo(PAGE);
            assertThat(responseDtos.getSize()).isEqualTo(SIZE);
        }
    }

    @Nested
    @DisplayName("update")
    class UpdateTest {

        @Test
        @DisplayName("update - 성공")
        void update_success() {
            // given
            UpdateTeamMemberRequestDto requestDto = new UpdateTeamMemberRequestDto(
                ROLE_VICE_LEADER);
            ReflectionTestUtils.setField(user, "id", 1L);

            TeamMember anotherTeamMember = new TeamMember(team, anotherUser, TeamMemberRole.MEMBER);

            when(teamRepository.findById(TEAM_ID)).thenReturn(Optional.of(team));
            when(teamMemberRepository.findByTeam_TeamIdAndUser_Id(TEAM_ID, USER_ID)).thenReturn(
                Optional.of(teamMember));
            when(teamMemberRepository.findByTeam_TeamIdAndUser_Id(TEAM_ID,
                ANOTHER_USER_ID)).thenReturn(
                Optional.of(anotherTeamMember));

            TeamMemberResponseDto expected = toResponse(anotherTeamMember);

            when(teamMemberMapper.toTeamMemberResponseDto(any(TeamMember.class))).thenReturn(
                expected);

            // when
            TeamMemberResponseDto resultDto = teamMemberService.update(TEAM_ID, ANOTHER_USER_ID,
                requestDto, USER_ID);

            // then
            assertThat(resultDto).isEqualTo(expected);
            assertThat(anotherTeamMember.getRole()).isEqualTo(TeamMemberRole.VICE_LEADER);
        }

        @Test
        @DisplayName("update - 팀 없음 예외")
        void update_teamNotFound_throws() {
            // given
            UpdateTeamMemberRequestDto requestDto = new UpdateTeamMemberRequestDto(
                ROLE_VICE_LEADER);

            when(teamRepository.findById(NON_EXISTENT_TEAM_ID)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() ->
                teamMemberService.update(NON_EXISTENT_TEAM_ID, USER_ID, requestDto, USER_ID))
                .isInstanceOf(NotFoundException.class);
        }

        @Test
        @DisplayName("update - 팀 멤버 없음 예외")
        void update_memberNotFound_throws() {
            // given
            UpdateTeamMemberRequestDto requestDto = new UpdateTeamMemberRequestDto(
                ROLE_VICE_LEADER);

            when(teamRepository.findById(TEAM_ID)).thenReturn(Optional.of(team));

            // when & then
            assertThatThrownBy(() ->
                teamMemberService.update(TEAM_ID, NON_EXISTENT_USER_ID, requestDto, USER_ID))
                .isInstanceOf(NotFoundException.class);
        }
    }

    @Nested
    @DisplayName("leave")
    class LeaveTest {

        @Test
        @DisplayName("leave - 성공")
        void leave_success() {
            // given
            TeamMember loginMember = new TeamMember(team, anotherUser, TeamMemberRole.MEMBER);
            team.recruitMember(anotherUser, TeamMemberRole.MEMBER);

            when(teamRepository.findById(TEAM_ID)).thenReturn(Optional.of(team));
            when(teamMemberRepository.findByTeam_TeamIdAndUser_Id(TEAM_ID,
                ANOTHER_USER_ID)).thenReturn(Optional.of(loginMember));

            // when
            teamMemberService.leave(TEAM_ID, ANOTHER_USER_ID);

            // then
            verify(teamRepository).save(team);
        }

        @Test
        @DisplayName("leave - 팀 없음 예외")
        void leave_teamNotFound_throws() {
            // given
            when(teamRepository.findById(NON_EXISTENT_TEAM_ID)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> teamMemberService.leave(NON_EXISTENT_TEAM_ID, USER_ID))
                .isInstanceOf(NotFoundException.class);
        }

        @Test
        @DisplayName("leave - 팀 멤버 없음 예외")
        void leave_memberNotFound_throws() {
            // given
            when(teamRepository.findById(TEAM_ID)).thenReturn(Optional.of(team));
            when(teamMemberRepository.findByTeam_TeamIdAndUser_Id(TEAM_ID,
                NON_EXISTENT_USER_ID)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> teamMemberService.leave(TEAM_ID, NON_EXISTENT_USER_ID))
                .isInstanceOf(NotFoundException.class);
        }

        @Test
        @DisplayName("leave - 회장 본인 탈퇴 금지")
        void leave_captain_forbidden() {
            // given
            ReflectionTestUtils.setField(anotherUser, "id", ANOTHER_USER_ID);
            TeamMember loginLeader = new TeamMember(team, anotherUser, TeamMemberRole.LEADER);

            when(teamRepository.findById(TEAM_ID)).thenReturn(Optional.of(team));
            when(teamMemberRepository.findByTeam_TeamIdAndUser_Id(TEAM_ID,
                ANOTHER_USER_ID)).thenReturn(Optional.of(loginLeader));

            // when & then
            assertThatThrownBy(() -> teamMemberService.leave(TEAM_ID, ANOTHER_USER_ID))
                .isInstanceOf(NoPermissionException.class);
        }
    }

    @Nested
    @DisplayName("kick")
    class KickTest {

        private Long leaderMemberId = 1L;
        private Long viceLeaderMemberId = 2L;

        private TeamMember leaderMember;
        private TeamMember viceLeaderMember;

        @BeforeEach
        void setUpKick() {
            ReflectionTestUtils.setField(user, "id", leaderMemberId);
            ReflectionTestUtils.setField(anotherUser, "id", viceLeaderMemberId);
        
            team.recruitMember(user, TeamMemberRole.LEADER);
            team.recruitMember(anotherUser, TeamMemberRole.VICE_LEADER);

            leaderMember = new TeamMember(team, user, TeamMemberRole.LEADER);
            viceLeaderMember = new TeamMember(team, anotherUser, TeamMemberRole.VICE_LEADER);
        }

        @Test
        @DisplayName("kick - 회장이 부회장 멤버 강퇴성공")
        void kick_success() {
            // given
            when(teamRepository.findById(TEAM_ID)).thenReturn(Optional.of(team));
            when(teamMemberRepository.findByTeam_TeamIdAndUser_Id(TEAM_ID,
                USER_ID)).thenReturn(Optional.of(leaderMember));
            when(teamMemberRepository.findByTeam_TeamIdAndUser_Id(TEAM_ID,
                ANOTHER_USER_ID)).thenReturn(Optional.of(viceLeaderMember));

            // when
            teamMemberService.kick(TEAM_ID, ANOTHER_USER_ID, USER_ID);

            // then
            verify(teamRepository).save(team);
        }

        @Test
        @DisplayName("kick - 일반 멤버는 강퇴 권한 없음")
        void kick_noPermission_whenActorIsMember() {
            // given
            TeamMember loginMember = new TeamMember(team, user, TeamMemberRole.MEMBER);
            TeamMember targetMember = new TeamMember(team, anotherUser, TeamMemberRole.MEMBER);

            when(teamRepository.findById(TEAM_ID)).thenReturn(Optional.of(team));
            when(teamMemberRepository.findByTeam_TeamIdAndUser_Id(TEAM_ID,
                USER_ID)).thenReturn(Optional.of(loginMember));
            when(teamMemberRepository.findByTeam_TeamIdAndUser_Id(TEAM_ID,
                ANOTHER_USER_ID)).thenReturn(Optional.of(targetMember));

            // when & then
            assertThatThrownBy(() -> teamMemberService.kick(TEAM_ID, ANOTHER_USER_ID, USER_ID))
                .isInstanceOf(NoPermissionException.class);
        }

        @Test
        @DisplayName("kick - 회장 강퇴 시도 금지 (대상=LEADER)")
        void kick_forbid_kickingLeader() {
            // given
            TeamMember actorLeader = new TeamMember(team, user, TeamMemberRole.LEADER);
            TeamMember targetLeader = new TeamMember(team, anotherUser, TeamMemberRole.LEADER);

            when(teamRepository.findById(TEAM_ID)).thenReturn(Optional.of(team));
            when(teamMemberRepository.findByTeam_TeamIdAndUser_Id(TEAM_ID,
                USER_ID)).thenReturn(Optional.of(actorLeader));
            when(teamMemberRepository.findByTeam_TeamIdAndUser_Id(TEAM_ID,
                ANOTHER_USER_ID)).thenReturn(Optional.of(targetLeader));

            // when & then
            assertThatThrownBy(() -> teamMemberService.kick(TEAM_ID, ANOTHER_USER_ID, USER_ID))
                .isInstanceOf(NoPermissionException.class);
        }

        @Test
        @DisplayName("kick - 부회장 강퇴는 회장만 가능")
        void kick_leaderCanKickViceLeader_success() {
            // given
            TeamMember actorLeader = new TeamMember(team, user, TeamMemberRole.LEADER);
            TeamMember targetVice = new TeamMember(team, anotherUser, TeamMemberRole.VICE_LEADER);

            when(teamRepository.findById(TEAM_ID)).thenReturn(Optional.of(team));
            when(teamMemberRepository.findByTeam_TeamIdAndUser_Id(TEAM_ID,
                USER_ID)).thenReturn(Optional.of(actorLeader));
            when(teamMemberRepository.findByTeam_TeamIdAndUser_Id(TEAM_ID,
                ANOTHER_USER_ID)).thenReturn(Optional.of(targetVice));
            
            // when
            teamMemberService.kick(TEAM_ID, ANOTHER_USER_ID, USER_ID);

            // then
            verify(teamRepository).save(team);
        }

        @Test
        @DisplayName("kick - 부회장 강퇴는 회장만 가능 (부회장은 불가)")
        void kick_viceLeaderCannotKickViceLeader_forbidden() {
            // given
            TeamMember actorVice = new TeamMember(team, user, TeamMemberRole.VICE_LEADER);
            TeamMember targetVice = new TeamMember(team, anotherUser, TeamMemberRole.VICE_LEADER);

            when(teamRepository.findById(TEAM_ID)).thenReturn(Optional.of(team));
            when(teamMemberRepository.findByTeam_TeamIdAndUser_Id(TEAM_ID,
                USER_ID)).thenReturn(Optional.of(actorVice));
            when(teamMemberRepository.findByTeam_TeamIdAndUser_Id(TEAM_ID,
                ANOTHER_USER_ID)).thenReturn(Optional.of(targetVice));

            // when & then
            assertThatThrownBy(() -> teamMemberService.kick(TEAM_ID, ANOTHER_USER_ID, USER_ID))
                .isInstanceOf(NoPermissionException.class);
        }
    }

    @Nested
    @DisplayName("delegateLeadership")
    class DelegateLeadershipTest {

        private Long currentLeaderId;

        private Long currentLeaderMemberId;
        private Long newLeaderMemberId;
        private Long anotherMemberId;

        private TeamMember currentLeader;
        private TeamMember newLeader;
        private TeamMember anotherMember;

        @BeforeEach
        void setUpDelegate() {

            currentLeaderId = 1L;

            currentLeader = new TeamMember(team, captain, TeamMemberRole.LEADER);
            newLeader = new TeamMember(team, user, TeamMemberRole.MEMBER);
            anotherMember = new TeamMember(team, anotherUser, TeamMemberRole.MEMBER);

            currentLeaderMemberId = 201L;
            newLeaderMemberId = 202L;
            anotherMemberId = 203L;

            ReflectionTestUtils.setField(currentLeader, "id", currentLeaderMemberId);
            ReflectionTestUtils.setField(newLeader, "id", newLeaderMemberId);
            ReflectionTestUtils.setField(anotherMember, "id", anotherMemberId);
        }

        @Test
        @DisplayName("delegateLeadership - 성공")
        void delegateLeadership_success() {
            // given
            TeamMemberResponseDto expected = toResponse(newLeader);

            when(teamRepository.findById(TEAM_ID)).thenReturn(Optional.of(team));
            when(teamMemberRepository.findByTeam_TeamIdAndUser_Id(TEAM_ID,
                currentLeaderId)).thenReturn(Optional.of(currentLeader));
            when(teamMemberRepository.findById(newLeaderMemberId)).thenReturn(
                Optional.of(newLeader));
            when(teamMemberMapper.toTeamMemberResponseDto(newLeader)).thenReturn(expected);

            // when
            TeamMemberResponseDto resultDto = teamMemberService.delegateLeadership(TEAM_ID,
                currentLeaderId, newLeaderMemberId);

            // then
            assertThat(resultDto).isEqualTo(expected);
            assertThat(currentLeader.getRole()).isEqualTo(TeamMemberRole.MEMBER);
            assertThat(newLeader.getRole()).isEqualTo(TeamMemberRole.LEADER);
        }

        @Test
        @DisplayName("delegateLeadership - 팀 없음 예외")
        void delegateLeadership_teamNotFound_throws() {
            // given
            when(teamRepository.findById(NON_EXISTENT_TEAM_ID)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(
                () -> teamMemberService.delegateLeadership(NON_EXISTENT_TEAM_ID, USER_ID, USER_ID))
                .isInstanceOf(NotFoundException.class);
        }

        @Test
        @DisplayName("delegateLeadership - 현재 리더 없음 예외")
        void delegateLeadership_currentUserNotFound_throws() {
            // given
            when(teamRepository.findById(TEAM_ID)).thenReturn(Optional.of(team));
            when(teamMemberRepository.findByTeam_TeamIdAndUser_Id(TEAM_ID,
                NON_EXISTENT_USER_ID)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(
                () -> teamMemberService.delegateLeadership(TEAM_ID, NON_EXISTENT_USER_ID,
                    newLeaderMemberId))
                .isInstanceOf(NotFoundException.class);
        }

        @Test
        @DisplayName("delegateLeadership - 새 리더 없음 예외")
        void delegateLeadership_newLeaderNotFound_throws() {
            // given
            Long NON_EXISTENT_MEMBER_ID = 9999L;

            when(teamRepository.findById(TEAM_ID)).thenReturn(Optional.of(team));
            when(teamMemberRepository.findByTeam_TeamIdAndUser_Id(TEAM_ID,
                currentLeaderId)).thenReturn(Optional.of(currentLeader));
            when(teamMemberRepository.findById(NON_EXISTENT_MEMBER_ID)).thenReturn(
                Optional.empty());

            // when & then
            assertThatThrownBy(() -> teamMemberService.delegateLeadership(TEAM_ID, currentLeaderId,
                NON_EXISTENT_MEMBER_ID))
                .isInstanceOf(NotFoundException.class);
        }

        @Test
        @DisplayName("delegateLeadership - 권한 없음 예외")
        void delegateLeadership_noPermission_throws() {
            // given
            when(teamRepository.findById(TEAM_ID)).thenReturn(Optional.of(team));
            when(teamMemberRepository.findByTeam_TeamIdAndUser_Id(TEAM_ID,
                anotherMemberId)).thenReturn(Optional.of(anotherMember));
            when(teamMemberRepository.findById(newLeaderMemberId)).thenReturn(
                Optional.of(newLeader));

            // when & then
            assertThatThrownBy(() -> teamMemberService.delegateLeadership(TEAM_ID, anotherMemberId,
                newLeaderMemberId))
                .isInstanceOf(NoPermissionException.class);
        }

        @Test
        @DisplayName("delegateLeadership - 자기 자신 위임 예외")
        void delegateLeadership_selfDelegation_throws() {
            // given
            when(teamRepository.findById(TEAM_ID)).thenReturn(Optional.of(team));
            when(teamMemberRepository.findByTeam_TeamIdAndUser_Id(TEAM_ID,
                currentLeaderId)).thenReturn(Optional.of(currentLeader));
            when(teamMemberRepository.findById(currentLeaderMemberId)).thenReturn(
                Optional.of(currentLeader));

            // when & then
            assertThatThrownBy(
                () -> teamMemberService.delegateLeadership(TEAM_ID, currentLeaderId,
                    currentLeaderMemberId))
                .isInstanceOf(DuplicatedException.class);
        }

        @Test
        @DisplayName("delegateLeadership - 다른 팀 멤버 위임 예외")
        void delegateLeadership_differentTeam_throws() {
            // given
            Long anotherTeamId = 2L;

            Team anotherTeam = new Team(
                "막국수 FC",
                captain,
                "강원대학교",
                TeamType.CENTRAL_CLUB,
                SkillLevel.AMATEUR,
                "춘천 유포리 막국수를 사랑하는 모임"
            );

            ReflectionTestUtils.setField(team, "teamId", 1L);
            ReflectionTestUtils.setField(anotherTeam, "teamId", 2L);

            Long anotherLeaderId = 4L;
            anotherTeam.recruitMember(captain, TeamMemberRole.LEADER);
            TeamMember anotherLeader = new TeamMember(anotherTeam, captain, TeamMemberRole.LEADER);

            when(teamRepository.findById(anotherTeamId)).thenReturn(Optional.of(anotherTeam));
            when(teamMemberRepository.findByTeam_TeamIdAndUser_Id(anotherTeamId,
                anotherLeaderId)).thenReturn(Optional.of(anotherLeader));
            when(teamMemberRepository.findById(anotherMemberId))
                .thenReturn(Optional.of(anotherMember));

            // when & then
            assertThatThrownBy(
                () -> teamMemberService.delegateLeadership(anotherTeamId, anotherLeaderId,
                    anotherMemberId))
                .isInstanceOf(DifferentException.class);
        }
    }

    private TeamMemberResponseDto toResponse(TeamMember member) {
        return new TeamMemberResponseDto(
            member.getId(),
            member.getUser().getId(),
            member.getUser().getName(),
            member.getUser().getEmail(),
            member.getUser().getPosition().toString(),
            member.getRole().toString(),
            member.getJoinedAt()
        );
    }
}
