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
import com.shootdoori.match.exception.DuplicatedException;
import com.shootdoori.match.exception.TeamMemberNotFoundException;
import com.shootdoori.match.exception.TeamNotFoundException;
import com.shootdoori.match.exception.UserNotFoundException;
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

        teamMember = new TeamMember(team, user, TeamMemberRole.LEADER);

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
            TeamMemberResponseDto resultDto = teamMemberService.create(TEAM_ID, requestDto);

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

            when(teamRepository.findById(TEAM_ID)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> teamMemberService.create(TEAM_ID, requestDto))
                .isInstanceOf(TeamNotFoundException.class);
        }

        @Test
        @DisplayName("create - 유저 없음 예외")
        void create_userNotFound_throws() {
            // given
            TeamMemberRequestDto requestDto = new TeamMemberRequestDto(
                USER_ID,
                ROLE_MEMBER
            );

            when(teamRepository.findById(TEAM_ID)).thenReturn(Optional.of(team));
            when(profileRepository.findById(USER_ID)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> teamMemberService.create(TEAM_ID, requestDto))
                .isInstanceOf(UserNotFoundException.class);
        }

        @Test
        @DisplayName("create - 이미 팀 멤버 예외")
        void create_alreadyMember_throws() {
            // given
            TeamMemberRequestDto requestDto = new TeamMemberRequestDto(
                USER_ID,
                ROLE_MEMBER
            );

            when(teamRepository.findById(TEAM_ID)).thenReturn(Optional.of(team));
            when(profileRepository.findById(USER_ID)).thenReturn(Optional.of(user));
            when(teamMemberRepository.existsByTeam_TeamIdAndUser_Id(TEAM_ID, USER_ID)).thenReturn(
                true);

            // when & then
            assertThatThrownBy(() -> teamMemberService.create(TEAM_ID, requestDto))
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
                .isInstanceOf(TeamMemberNotFoundException.class);
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

            TeamMemberResponseDto expected = toResponse(teamMember);

            when(teamRepository.findById(TEAM_ID)).thenReturn(Optional.of(team));
            when(teamMemberRepository.findByTeam_TeamIdAndUser_Id(TEAM_ID, USER_ID)).thenReturn(
                Optional.of(teamMember));
            when(teamMemberMapper.toTeamMemberResponseDto(any(TeamMember.class))).thenReturn(
                expected);

            // when
            TeamMemberResponseDto resultDto = teamMemberService.update(TEAM_ID, USER_ID,
                requestDto);

            // then
            assertThat(resultDto).isEqualTo(expected);
            assertThat(teamMember.getRole()).isEqualTo(TeamMemberRole.VICE_LEADER);
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
                teamMemberService.update(NON_EXISTENT_TEAM_ID, USER_ID, requestDto))
                .isInstanceOf(TeamNotFoundException.class);
        }

        @Test
        @DisplayName("update - 팀 멤버 없음 예외")
        void update_memberNotFound_throws() {
            // given
            UpdateTeamMemberRequestDto requestDto = new UpdateTeamMemberRequestDto(
                ROLE_VICE_LEADER);

            when(teamRepository.findById(TEAM_ID)).thenReturn(Optional.of(team));
            when(teamMemberRepository.findByTeam_TeamIdAndUser_Id(TEAM_ID,
                NON_EXISTENT_USER_ID)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() ->
                teamMemberService.update(TEAM_ID, NON_EXISTENT_USER_ID, requestDto))
                .isInstanceOf(TeamMemberNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("delete")
    class DeleteTest {

        @Test
        @DisplayName("delete - 성공")
        void delete_success() {
            // given
            TeamMember anotherTeamMember = new TeamMember(team, anotherUser, TeamMemberRole.MEMBER);
            team.recruitMember(anotherUser, TeamMemberRole.MEMBER);

            when(teamRepository.findById(TEAM_ID)).thenReturn(Optional.of(team));
            when(teamMemberRepository.findByTeam_TeamIdAndUser_Id(TEAM_ID,
                ANOTHER_USER_ID)).thenReturn(Optional.of(anotherTeamMember));

            // when
            teamMemberService.delete(TEAM_ID, ANOTHER_USER_ID);

            // then
            verify(teamRepository).save(team);
        }

        @Test
        @DisplayName("delete - 팀 없음 예외")
        void delete_teamNotFound_throws() {
            // given
            when(teamRepository.findById(NON_EXISTENT_TEAM_ID)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> teamMemberService.delete(NON_EXISTENT_TEAM_ID, USER_ID))
                .isInstanceOf(TeamNotFoundException.class);
        }

        @Test
        @DisplayName("delete - 팀 멤버 없음 예외")
        void delete_memberNotFound_throws() {
            // given
            when(teamRepository.findById(TEAM_ID)).thenReturn(Optional.of(team));
            when(teamMemberRepository.findByTeam_TeamIdAndUser_Id(TEAM_ID,
                NON_EXISTENT_USER_ID)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> teamMemberService.delete(TEAM_ID, NON_EXISTENT_USER_ID))
                .isInstanceOf(TeamMemberNotFoundException.class);
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
