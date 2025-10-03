package com.shootdoori.profile;

import com.shootdoori.match.dto.ProfileCreateRequest;
import com.shootdoori.match.dto.ProfileMapper;
import com.shootdoori.match.dto.ProfileResponse;
import com.shootdoori.match.dto.ProfileUpdateRequest;
import com.shootdoori.match.entity.*;
import com.shootdoori.match.exception.common.DuplicatedException;
import com.shootdoori.match.repository.ProfileRepository;
import com.shootdoori.match.repository.TeamMemberRepository;
import com.shootdoori.match.service.ProfileService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ProfileTest {

    @Mock private ProfileRepository profileRepository;
    @Mock private TeamMemberRepository teamMemberRepository;
    @Mock private ProfileMapper profileMapper;
    @Mock private PasswordEncoder passwordEncoder;

    @InjectMocks
    private ProfileService profileService;

    private User user;
    private ProfileCreateRequest createRequest;
    private final Long userId = 1L;

    @BeforeEach
    void setup() {
        createRequest = new ProfileCreateRequest(
            "jam", "아마추어", "test@email.com", "test@ac.kr",
            "asdf02~!", "imkim2511", "공격수", "knu", "cs",
            "20", "hello, world"
        );

        user = User.create(
            createRequest.name(), createRequest.skillLevel(), createRequest.email(), createRequest.universityEmail(),
            createRequest.password(), createRequest.kakaoTalkId(), createRequest.position(), createRequest.university(),
            createRequest.department(), createRequest.studentYear(), createRequest.bio()
        );
    }

    @Test
    @DisplayName("프로필 정보 수정 성공")
    void updateProfile_Success() {
        // given
        ProfileUpdateRequest updateRequest = new ProfileUpdateRequest("jam", "프로", "골키퍼", "변경된 자기소개");
        when(profileRepository.findById(userId)).thenReturn(Optional.of(user));
        when(teamMemberRepository.findByUser_Id(userId)).thenReturn(Optional.empty());

        ProfileResponse expectedResponse = new ProfileResponse(
            "jam",
            "PRO",
            "test@email.com",
            "imkim2511",
            "GK",
            "knu",
            "cs",
            "20",
            "변경된 자기소개",
            user.getCreatedAt(),
            null
        );
        when(profileMapper.toProfileResponse(user, null)).thenReturn(expectedResponse);

        // when
        ProfileResponse actualResponse = profileService.updateProfile(userId, updateRequest);

        // then
        assertThat(user.getName()).isEqualTo("jam");
        assertThat(user.getSkillLevel()).isEqualTo(SkillLevel.PRO);
        assertThat(user.getPosition()).isEqualTo(UserPosition.GK);
        assertThat(user.getBio()).isEqualTo("변경된 자기소개");

        assertThat(actualResponse).isNotNull();
        assertThat(actualResponse.skillLevel()).isEqualTo("PRO");
        assertThat(actualResponse.position()).isEqualTo("GK");
        assertThat(actualResponse.bio()).isEqualTo("변경된 자기소개");
    }

    @Test
    @DisplayName("프로필 생성 성공")
    void createProfile_Success() {
        // given
        when(profileRepository.existsByEmailOrUniversityEmail(createRequest.email(), createRequest.universityEmail()))
            .thenReturn(false);

        when(passwordEncoder.encode(any(String.class))).thenReturn("encodedPassword");

        when(profileRepository.save(any(User.class))).thenReturn(user);

        when(profileMapper.toProfileResponse(user)).thenReturn(
            new ProfileResponse("jam", "AMATEUR", "test@email.com", "imkim25",
                "FW", "knu", "cs", "20", "hello, world", LocalDateTime.now(), null)
        );

        // when
        ProfileResponse response = profileService.createProfile(createRequest);

        // then
        assertThat(response).isNotNull();
        assertThat(response.name()).isEqualTo(createRequest.name());
        assertThat(response.skillLevel()).isEqualTo(SkillLevel.AMATEUR.name());
        assertThat(response.position()).isEqualTo(UserPosition.FW.name());
        verify(profileRepository).save(any(User.class));
    }

    @Test
    @DisplayName("프로필 생성 실패 - 유효하지 않은 포지션")
    void createProfile_WithInvalidPosition_ThrowsException() {
        // given
        ProfileCreateRequest invalidRequest = new ProfileCreateRequest(
            "jam", "아마추어", "new@email.com", "new@ac.kr",
            "asdf02~!", "imkim2512", "마법사", "knu", "cs",
            "20", "hello, world"
        );

        // when & then
        assertThrows(IllegalArgumentException.class, () -> profileService.createProfile(invalidRequest));
    }

    @Test
    @DisplayName("이미 존재하는 이메일로 프로필 생성 시 DuplicatedUserException 예외 발생")
    void createProfile_WithDuplicateEmail_ThrowsException() {
        // given
        ProfileCreateRequest duplicateRequest = new ProfileCreateRequest(
            "jam", "아마추어", "duplicate@email.com", "test@kangwon.ac.kr",
            "asdf02~!","imkim2513", "공격수", "knu", "cs",
            "20", "hello, world"
        );

        when(profileRepository.existsByEmailOrUniversityEmail(
            duplicateRequest.email(),
            duplicateRequest.universityEmail()
        )).thenReturn(true);

        // when & then
        assertThrows(DuplicatedException.class, () -> profileService.createProfile(duplicateRequest));
    }

    @Test
    @DisplayName("팀에 속한 사용자 프로필 조회 성공 (teamId 포함)")
    void findProfileById_WithTeam_Success() {
        // given
        Team team = new Team("팀이름", user, "knu", TeamType.OTHER, SkillLevel.AMATEUR, "설명");
        Long teamId = 123L;
        ReflectionTestUtils.setField(team, "teamId", teamId);
        TeamMember teamMember = new TeamMember(team, user, TeamMemberRole.MEMBER);

        ProfileResponse expectedResponse = new ProfileResponse(
            "jam", "AMATEUR", "test@email.com", "imkim25", "FW",
            "knu", "cs", "20", "hello, world", LocalDateTime.now(), teamId);

        when(profileRepository.findById(userId)).thenReturn(Optional.of(user));
        when(teamMemberRepository.findByUser_Id(userId)).thenReturn(Optional.of(teamMember));
        when(profileMapper.toProfileResponse(user, teamId)).thenReturn(expectedResponse);

        // when
        ProfileResponse actualResponse = profileService.findProfileById(userId);

        // then
        assertThat(actualResponse).isNotNull();
        assertThat(actualResponse.teamId()).isEqualTo(teamId);
        verify(teamMemberRepository).findByUser_Id(userId);
    }

    @Test
    @DisplayName("팀에 속하지 않은 사용자 프로필 조회 성공 (teamId는 null)")
    void findProfileById_WithoutTeam_Success() {
        // given
        ProfileResponse expectedResponse = new ProfileResponse(
            "jam", "AMATEUR", "test@email.com", "imkim25", "FW",
            "knu", "cs", "20", "hello, world", LocalDateTime.now(), null);

        when(profileRepository.findById(userId)).thenReturn(Optional.of(user));
        when(teamMemberRepository.findByUser_Id(userId)).thenReturn(Optional.empty());
        when(profileMapper.toProfileResponse(user, null)).thenReturn(expectedResponse);

        // when
        ProfileResponse actualResponse = profileService.findProfileById(userId);

        // then
        assertThat(actualResponse).isNotNull();
        assertThat(actualResponse.teamId()).isNull();
        verify(teamMemberRepository).findByUser_Id(userId);
    }
}
