package com.shootdoori.team;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.shootdoori.match.dto.CreateTeamResponseDto;
import com.shootdoori.match.dto.TeamDetailResponseDto;
import com.shootdoori.match.dto.TeamMapper;
import com.shootdoori.match.dto.TeamRequestDto;
import com.shootdoori.match.entity.SkillLevel;
import com.shootdoori.match.entity.Team;
import com.shootdoori.match.entity.TeamType;
import com.shootdoori.match.entity.User;
import com.shootdoori.match.exception.CaptainNotFoundException;
import com.shootdoori.match.exception.TeamNotFoundException;
import com.shootdoori.match.repository.ProfileRepository;
import com.shootdoori.match.repository.TeamRepository;
import com.shootdoori.match.service.TeamService;
import com.shootdoori.match.value.UniversityName;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
@DisplayName("TeamService 테스트")
public class TeamServiceTest {

    private static final Logger log = LoggerFactory.getLogger(TeamServiceTest.class);
    private static final Long TEAM_ID = 1L;
    private static final Long NON_EXISTENT_TEAM_ID = 100L;
    private static final int PAGE = 0;
    private static final int SIZE = 10;
    private static final String UNIVERSITY = "강원대학교";
    @Mock
    private ProfileRepository profileRepository;

    @Mock
    private TeamRepository teamRepository;

    @Mock
    private TeamMapper teamMapper;

    private TeamService teamService;
    private TeamRequestDto requestDto;
    private User captain;
    private User newMember;

    @BeforeEach
    void setUp() {
        teamService = new TeamService(profileRepository, teamRepository, teamMapper);

        requestDto = new TeamRequestDto(
            "강원대 FC",
            "강원대 1위 팀 먹겠습니다.",
            "강원대학교",
            "아마추어",
            "과동아리"
        );

        captain = User.create(
            "김학생",
            "아마추어",
            "student@example.com",
            "student@kangwon.ac.kr",
            "Abcd1234!",
            "imkim2501",
            "골키퍼",
            "강원대학교",
            "컴퓨터공학과",
            "25",
            "축구를 좋아하는 대학생입니다. 골키퍼 포지션을 주로 맡고 있으며, 즐겁게 운동하고 싶습니다!"
        );

        newMember = User.create(
            "손응민",
            "세미프로",
            "student999@gmail.com",
            "student35@kangwon.ac.kr",
            "Abcd1234!",
            "imkim2502",
            "풀백",
            "강원대학교",
            "컴퓨터공학과",
            "35",
            "축구 좋아하는 아빠입니다."
        );
    }

    @Test
    @DisplayName("create - 정상 생성")
    void create_success() {

        // given
        ReflectionTestUtils.setField(captain, "id", 1L);
        ReflectionTestUtils.setField(newMember, "id", 2L);

        CreateTeamResponseDto createResponseDto = new CreateTeamResponseDto(
            TEAM_ID,
            "팀이 성공적으로 생성되었습니다.",
            "/api/teams/" + TEAM_ID
        );

        Team savedTeam = createTeam(
            "강원대 FC",
            TeamType.fromDisplayName("과동아리"),
            SkillLevel.fromDisplayName("아마추어"),
            "주 2회 연습합니다."
        );

        ReflectionTestUtils.setField(savedTeam, "teamId", TEAM_ID);

        // TODO: JWT 토큰 도입 이후 필요 없는 코드 - save(captain)
        when(profileRepository.save(captain)).thenReturn(captain);
        when(teamRepository.save(any(Team.class))).thenReturn(savedTeam);
        when(teamMapper.toCreateTeamResponse(savedTeam)).thenReturn(createResponseDto);

        // when
        CreateTeamResponseDto resultDto = teamService.create(requestDto, captain);

        // then
        assertThat(resultDto).isEqualTo(createResponseDto);
        assertThat(resultDto.teamId()).isEqualTo(TEAM_ID);
        assertThat(resultDto.message()).isEqualTo("팀이 성공적으로 생성되었습니다.");
        assertThat(resultDto.teamUrl()).isEqualTo("/api/teams/" + TEAM_ID);
    }

    @Test
    @DisplayName("create - captain null이면 예외")
    void create_nullCaptain_throws() {
        // given
        User nullCaptain = null;

        // when & then
        assertThatThrownBy(() ->
            teamService.create(requestDto, nullCaptain))
            .isInstanceOf(CaptainNotFoundException.class);
    }

    @Test
    @DisplayName("findById - 성공")
    void findById_success() {
        // given
        Team mockTeam = createTeam(
            "강원대 FC",
            TeamType.fromDisplayName("과동아리"),
            SkillLevel.fromDisplayName("아마추어"),
            "주 2회 연습합니다."
        );

        TeamDetailResponseDto expectedResponseDto = new TeamDetailResponseDto(
            TEAM_ID,
            "강원대 FC",
            "강원대 1위 팀 먹겠습니다.",
            "강원대학교",
            SkillLevel.AMATEUR,
            TeamType.DEPARTMENT_CLUB,
            1,
            "2024-01-01T00:00:00"
        );

        ReflectionTestUtils.setField(mockTeam, "teamId", TEAM_ID);

        when(teamRepository.findById(TEAM_ID)).thenReturn(Optional.of(mockTeam));
        when(teamMapper.toTeamDetailResponse(mockTeam)).thenReturn(expectedResponseDto);

        // when
        TeamDetailResponseDto resultDto = teamService.findById(TEAM_ID);

        // then
        assertThat(resultDto).isNotNull();
        assertThat(resultDto.id()).isEqualTo(TEAM_ID);
        assertThat(resultDto.name()).isEqualTo("강원대 FC");
        assertThat(resultDto.university()).isEqualTo("강원대학교");
        assertThat(resultDto.skillLevel()).isEqualTo(SkillLevel.AMATEUR);
        assertThat(resultDto.teamType()).isEqualTo(TeamType.DEPARTMENT_CLUB);
        assertThat(resultDto.memberCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("findById - 팀 없음 예외")
    void findById_notFound_throws() {
        // given
        when(teamRepository.findById(NON_EXISTENT_TEAM_ID)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() ->
            teamService.findById(NON_EXISTENT_TEAM_ID))
            .isInstanceOf(TeamNotFoundException.class);
    }

    @Test
    @DisplayName("findAllByUniversity - 페이징/정렬 및 매핑")
    void findAllByUniversity_success() {
        // given
        Team team1 = createTeam(
            "강원대 FC",
            TeamType.fromDisplayName("과동아리"),
            SkillLevel.fromDisplayName("아마추어"),
            "주 2회 연습합니다."
        );

        Team team2 = createTeam(
            "감자의 신 FC",
            TeamType.fromDisplayName("중앙동아리"),
            SkillLevel.fromDisplayName("세미프로"),
            "감자빵이 맛있어요."
        );

        List<Team> teamList = Arrays.asList(team1, team2);
        Page<Team> teamPage = new PageImpl<>(teamList,
            PageRequest.of(PAGE, SIZE, Sort.by("teamName").ascending()), 2);

        TeamDetailResponseDto response1 = new TeamDetailResponseDto(1L, "강원대 FC", "주 2회 연습합니다.",
            "강원대학교", SkillLevel.AMATEUR, TeamType.DEPARTMENT_CLUB, 1, "2025-09-25T00:00:00");
        TeamDetailResponseDto response2 = new TeamDetailResponseDto(2L, "감자의 신 FC", "감자빵이 맛있어요.",
            "강원대학교", SkillLevel.SEMI_PRO, TeamType.CENTRAL_CLUB, 1, "2025-09-25T00:00:00");

        when(teamRepository.findAllByUniversity(any(UniversityName.class), any(Pageable.class)))
            .thenReturn(teamPage);
        when(teamMapper.toTeamDetailResponse(team1)).thenReturn(response1);
        when(teamMapper.toTeamDetailResponse(team2)).thenReturn(response2);

        // when
        Page<TeamDetailResponseDto> responseDtos = teamService.findAllByUniversity(PAGE, SIZE,
            UNIVERSITY);

        // then
        assertThat(responseDtos).isNotNull();
        assertThat(responseDtos.getContent()).hasSize(2);
        assertThat(responseDtos.getTotalElements()).isEqualTo(2);
        assertThat(responseDtos.getNumber()).isEqualTo(PAGE);
        assertThat(responseDtos.getSize()).isEqualTo(SIZE);
    }

    @Test
    @DisplayName("update - 성공")
    void update_success() {
        TeamRequestDto updateRequestDto = new TeamRequestDto(
            "수정된 팀명",
            "수정된 설명",
            "강원대학교",
            "세미프로",
            "중앙동아리"
        );

        Team existingTeam = createTeam(
            "강원대 FC",
            TeamType.fromDisplayName("과동아리"),
            SkillLevel.fromDisplayName("아마추어"),
            "주 2회 연습합니다."
        );

        TeamDetailResponseDto updatedResponseDto = new TeamDetailResponseDto(
            TEAM_ID,
            "수정된 팀명",
            "수정된 설명",
            "강원대학교",
            SkillLevel.SEMI_PRO,
            TeamType.CENTRAL_CLUB,
            1,
            "2024-01-01T00:00:00"
        );

        when(teamRepository.findById(TEAM_ID)).thenReturn(Optional.of(existingTeam));
        when(teamMapper.toTeamDetailResponse(existingTeam)).thenReturn(updatedResponseDto);

        // when
        TeamDetailResponseDto resultDto = teamService.update(TEAM_ID, updateRequestDto);

        // then
        assertThat(resultDto).isNotNull();
        assertThat(resultDto.id()).isEqualTo(TEAM_ID);
        assertThat(resultDto.name()).isEqualTo("수정된 팀명");
        assertThat(resultDto.description()).isEqualTo("수정된 설명");
        assertThat(resultDto.skillLevel()).isEqualTo(SkillLevel.SEMI_PRO);
    }

    @Test
    @DisplayName("update - 팀 없음 예외")
    void update_notFound_throws() {
        // given
        TeamRequestDto updateRequestDto = new TeamRequestDto(
            "수정된 팀명",
            "수정된 설명",
            "강원대학교",
            "세미프로",
            "중앙동아리"
        );

        when(teamRepository.findById(NON_EXISTENT_TEAM_ID)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() ->
            teamService.update(NON_EXISTENT_TEAM_ID, updateRequestDto))
            .isInstanceOf(TeamNotFoundException.class);
    }

    @Test
    @DisplayName("delete - 성공")
    void delete_success() {
        // given
        Team existingTeam = createTeam(
            "삭제될 팀",
            TeamType.CENTRAL_CLUB,
            SkillLevel.AMATEUR,
            "삭제될 팀입니다."
        );

        when(teamRepository.findById(TEAM_ID)).thenReturn(Optional.of(existingTeam));

        // when
        teamService.delete(TEAM_ID);

        // then
        verify(teamRepository).findById(TEAM_ID);
        verify(teamRepository).delete(existingTeam);
    }

    @Test
    @DisplayName("delete - 팀 없음 예외")
    void delete_notFound_throws() {
        // given
        when(teamRepository.findById(NON_EXISTENT_TEAM_ID)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> teamService.delete(NON_EXISTENT_TEAM_ID))
            .isInstanceOf(TeamNotFoundException.class);
    }

    private Team createTeam(String name, TeamType teamType, SkillLevel skillLevel,
        String description) {
        return new Team(
            name,
            captain,
            UNIVERSITY,
            teamType,
            skillLevel,
            description
        );
    }
}