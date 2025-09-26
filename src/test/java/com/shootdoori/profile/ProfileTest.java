package com.shootdoori.profile;

import com.shootdoori.match.dto.ProfileCreateRequest;
import com.shootdoori.match.dto.ProfileMapper;
import com.shootdoori.match.dto.ProfileResponse;
import com.shootdoori.match.dto.ProfileUpdateRequest;
import com.shootdoori.match.entity.Position;
import com.shootdoori.match.entity.SkillLevel;
import com.shootdoori.match.entity.User;
import com.shootdoori.match.exception.DuplicatedUserException;
import com.shootdoori.match.repository.ProfileRepository;
import com.shootdoori.match.service.ProfileService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ProfileTest {

    @Mock
    private ProfileRepository profileRepository;

    @Mock
    private ProfileMapper profileMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

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

        // when
        profileService.updateProfile(userId, updateRequest);

        // then
        assertThat(user.getSkillLevel()).isEqualTo(SkillLevel.PRO);
        assertThat(user.getPosition()).isEqualTo(Position.GK);
        assertThat(user.getBio()).isEqualTo("변경된 자기소개");
        assertThat(user.getName()).isEqualTo("jam");
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
                "FW", "knu", "cs", "20", "hello, world", LocalDateTime.now())
        );

        // when
        ProfileResponse response = profileService.createProfile(createRequest);

        // then
        assertThat(response).isNotNull();
        assertThat(response.name()).isEqualTo(createRequest.name());
        assertThat(response.skillLevel()).isEqualTo(SkillLevel.AMATEUR.name());
        assertThat(response.position()).isEqualTo(Position.FW.name());
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
        assertThrows(DuplicatedUserException.class, () -> profileService.createProfile(duplicateRequest));
    }
}
