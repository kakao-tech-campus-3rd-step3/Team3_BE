package com.shootdoori.profile;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.shootdoori.match.dto.ProfileCreateRequest;
import com.shootdoori.match.dto.ProfileMapper;
import com.shootdoori.match.dto.ProfileResponse;
import com.shootdoori.match.dto.ProfileUpdateRequest;
import com.shootdoori.match.entity.team.Team;
import com.shootdoori.match.entity.team.TeamMember;
import com.shootdoori.match.entity.team.TeamMemberRole;
import com.shootdoori.match.entity.team.TeamSkillLevel;
import com.shootdoori.match.entity.team.TeamType;
import com.shootdoori.match.entity.user.User;
import com.shootdoori.match.entity.user.UserPosition;
import com.shootdoori.match.entity.user.UserSkillLevel;
import com.shootdoori.match.entity.user.UserStatus;
import com.shootdoori.match.exception.LeaderCannotLeaveTeamException;
import com.shootdoori.match.exception.common.DuplicatedException;
import com.shootdoori.match.exception.common.ErrorCode;
import com.shootdoori.match.exception.common.NotFoundException;
import com.shootdoori.match.repository.ProfileRepository;
import com.shootdoori.match.repository.RefreshTokenRepository;
import com.shootdoori.match.repository.TeamMemberRepository;
import com.shootdoori.match.service.ProfileService;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class ProfileTest {

    @Mock
    private ProfileRepository profileRepository;
    @Mock
    private TeamMemberRepository teamMemberRepository;
    @Mock
    private ProfileMapper profileMapper;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @InjectMocks
    private ProfileService profileService;

    @Nested
    @DisplayName("프로필 생성")
    class CreateProfile {

        @Test
        @DisplayName("프로필 생성 성공")
        void createProfile_Success() {
            // given
            ProfileCreateRequest request = createProfileRequest();
            User user = createUser(request);

            given(profileRepository.existsByEmail(request.email())).willReturn(false);
            given(passwordEncoder.encode(request.password())).willReturn("encodedPassword");
            given(profileRepository.save(any(User.class))).willReturn(user);
            given(profileMapper.toProfileResponse(user)).willReturn(
                new ProfileResponse("jam", "AMATEUR", "test@any.ac.kr", "imkim25",
                    "FW", "knu", "cs", "20", "hello, world", LocalDateTime.now(), null)
            );

            // when
            ProfileResponse response = profileService.createProfile(request);

            // then
            assertThat(response).isNotNull();
            assertThat(response.name()).isEqualTo(request.name());
            assertThat(response.skillLevel()).isEqualTo(UserSkillLevel.AMATEUR.name());
            assertThat(response.position()).isEqualTo(UserPosition.FW.name());
            verify(profileRepository).save(any(User.class));
        }

        @Test
        @DisplayName("프로필 생성 실패 - 중복된 이메일")
        void createProfile_Fail_DuplicateEmail() {
            // given
            ProfileCreateRequest request = createProfileRequest();

            given(profileRepository.existsByEmail(request.email())).willReturn(true);

            // when & then
            assertThatThrownBy(() -> profileService.createProfile(request))
                .isInstanceOf(DuplicatedException.class)
                .hasMessageContaining(ErrorCode.DUPLICATED_USER.getMessage());
        }

        @Test
        @DisplayName("프로필 생성 실패 - 유효하지 않은 포지션")
        void createProfile_Fail_InvalidPosition() {
            // given
            ProfileCreateRequest request = new ProfileCreateRequest(
                "jam", "아마추어", "new@any.ac.kr",
                "asdf02~!", "imkim2512", "마법사", "knu", "cs",
                "20", "hello, world"
            );

            // when & then
            assertThatThrownBy(() -> profileService.createProfile(request))
                .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("프로필 조회")
    class FindProfile {

        @Test
        @DisplayName("프로필 조회 성공 - 팀 소속")
        void findProfileById_Success_WithTeam() {
            // given
            Long userId = 1L;
            User user = createUser(createProfileRequest());
            Team team = createTeam(user);
            Long teamId = 123L;
            ReflectionTestUtils.setField(team, "teamId", teamId);
            TeamMember teamMember = new TeamMember(team, user, TeamMemberRole.MEMBER);

            ProfileResponse expectedResponse = new ProfileResponse(
                "jam", "AMATEUR", "test@email.ac.kr", "imkim25", "FW",
                "knu", "cs", "20", "hello, world", LocalDateTime.now(), teamId);

            given(profileRepository.findById(userId)).willReturn(Optional.of(user));
            given(teamMemberRepository.findByUser_Id(userId)).willReturn(Optional.of(teamMember));
            given(profileMapper.toProfileResponse(user, teamId)).willReturn(expectedResponse);

            // when
            ProfileResponse response = profileService.findProfileById(userId);

            // then
            assertThat(response).isNotNull();
            assertThat(response.teamId()).isEqualTo(teamId);
            verify(teamMemberRepository).findByUser_Id(userId);
        }

        @Test
        @DisplayName("프로필 조회 성공 - 팀 미소속")
        void findProfileById_Success_WithoutTeam() {
            // given
            Long userId = 1L;
            User user = createUser(createProfileRequest());
            ProfileResponse expectedResponse = new ProfileResponse(
                "jam", "AMATEUR", "test@email.ac.kr", "imkim25", "FW",
                "knu", "cs", "20", "hello, world", LocalDateTime.now(), null);

            given(profileRepository.findById(userId)).willReturn(Optional.of(user));
            given(teamMemberRepository.findByUser_Id(userId)).willReturn(Optional.empty());
            given(profileMapper.toProfileResponse(user, null)).willReturn(expectedResponse);

            // when
            ProfileResponse response = profileService.findProfileById(userId);

            // then
            assertThat(response).isNotNull();
            assertThat(response.teamId()).isNull();
            verify(teamMemberRepository).findByUser_Id(userId);
        }
    }

    @Nested
    @DisplayName("프로필 수정")
    class UpdateProfile {

        @Test
        @DisplayName("프로필 수정 성공")
        void updateProfile_Success() {
            // given
            Long userId = 1L;
            User user = createUser(createProfileRequest());
            ProfileUpdateRequest updateRequest = new ProfileUpdateRequest("jam", "프로", "골키퍼",
                "변경된 자기소개");

            given(profileRepository.findById(userId)).willReturn(Optional.of(user));
            given(teamMemberRepository.findByUser_Id(userId)).willReturn(Optional.empty());
            given(profileMapper.toProfileResponse(user, null)).willReturn(
                new ProfileResponse("jam", "PRO", "test@any.ac.kr", "imkim2511",
                    "GK", "knu", "cs", "20", "변경된 자기소개", user.getCreatedAt(), null)
            );

            // when
            ProfileResponse response = profileService.updateProfile(userId, updateRequest);

            // then
            assertThat(user.getSkillLevel()).isEqualTo(UserSkillLevel.PRO);
            assertThat(user.getPosition()).isEqualTo(UserPosition.GK);
            assertThat(user.getBio()).isEqualTo("변경된 자기소개");
            assertThat(response.skillLevel()).isEqualTo("PRO");
            assertThat(response.position()).isEqualTo("GK");
            assertThat(response.bio()).isEqualTo("변경된 자기소개");
        }
    }

    @Nested
    @DisplayName("회원 탈퇴")
    class DeleteAccount {

        @Test
        @DisplayName("회원 탈퇴 성공 - 팀에 소속되지 않은 경우")
        void deleteAccount_Success_NoTeam() {
            // given
            Long userId = 1L;
            User user = createUser(createProfileRequest());

            given(profileRepository.findById(userId)).willReturn(Optional.of(user));
            given(teamMemberRepository.findByUser_Id(userId)).willReturn(Optional.empty());

            // when
            profileService.deleteAccount(userId);

            // then
            assertThat(user.getStatus()).isEqualTo(UserStatus.DELETED);
            verify(teamMemberRepository, times(1)).findByUser_Id(userId);
            verify(refreshTokenRepository, times(1)).deleteAllByUserId(userId);
        }

        @Test
        @DisplayName("회원 탈퇴 성공 - 팀에 소속되었지만 회장이 아닌 경우")
        void deleteAccount_Success_TeamMemberNotCaptain() {
            // given
            Long userId = 1L;
            User user = createUser(createProfileRequest());
            User captain = mock(User.class);
            Team team = mock(Team.class);
            TeamMember teamMember = mock(TeamMember.class);

            given(profileRepository.findById(userId)).willReturn(Optional.of(user));
            given(teamMemberRepository.findByUser_Id(userId)).willReturn(Optional.of(teamMember));
            given(teamMember.getTeam()).willReturn(team);
            given(team.getCaptain()).willReturn(captain);

            // when
            profileService.deleteAccount(userId);

            // then
            assertThat(user.getStatus()).isEqualTo(UserStatus.DELETED);

            verify(refreshTokenRepository, times(1)).deleteAllByUserId(userId);
        }

        @Test
        @DisplayName("회원 탈퇴 실패 - 존재하지 않는 사용자")
        void deleteAccount_Fail_UserNotFound() {
            // given
            Long userId = 999L;
            given(profileRepository.findById(userId)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> profileService.deleteAccount(userId))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining(ErrorCode.PROFILE_NOT_FOUND.getMessage());

            verify(refreshTokenRepository, never()).deleteAllByUserId(any());
        }

        @Test
        @DisplayName("회원 탈퇴 실패 - 팀 회장인 경우")
        void deleteAccount_Fail_UserIsCaptain() {
            // given
            Long userId = 1L;
            User user = createUser(createProfileRequest());
            Team team = mock(Team.class);
            TeamMember teamMember = mock(TeamMember.class);

            given(profileRepository.findById(userId)).willReturn(Optional.of(user));
            given(teamMemberRepository.findByUser_Id(userId)).willReturn(Optional.of(teamMember));
            given(teamMember.getTeam()).willReturn(team);
            given(team.getCaptain()).willReturn(user);

            // when & then
            assertThatThrownBy(() -> profileService.deleteAccount(userId))
                .isInstanceOf(LeaderCannotLeaveTeamException.class)
                .hasMessageContaining(ErrorCode.LEADER_CANNOT_LEAVE_TEAM.getMessage());

            assertThat(user.getStatus()).isEqualTo(UserStatus.ACTIVE);

            verify(refreshTokenRepository, never()).deleteAllByUserId(any());
        }
    }

    // Test Fixtures
    private ProfileCreateRequest createProfileRequest() {
        return new ProfileCreateRequest(
            "jam", "아마추어", "test@any.ac.kr",
            "asdf02~!", "imkim2511", "공격수", "knu", "cs",
            "20", "hello, world"
        );
    }

    private User createUser(ProfileCreateRequest request) {
        return User.create(
            request.name(), request.skillLevel(), request.email(),
            request.password(), request.kakaoTalkId(), request.position(), request.university(),
            request.department(), request.studentYear(), request.bio()
        );
    }

    private Team createTeam(User captain) {
        return new Team("팀이름", captain, "knu", TeamType.OTHER, TeamSkillLevel.AMATEUR, "설명");
    }
}